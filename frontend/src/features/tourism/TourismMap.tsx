import { AdvancedMarker, APIProvider, InfoWindow, Map, useMap } from '@vis.gl/react-google-maps'
import { House } from 'lucide-react'
import { useEffect, useMemo, useRef, useState, type CSSProperties } from 'react'
import { useTranslation } from 'react-i18next'
import type { Language } from '../../i18n/languages'
import type { PublicAttraction, PublicStarTourRoute } from '../../shared/api/tourism'
import AttractionCategoryIcon from './AttractionCategoryIcon'
import { categoryForAttraction } from './tourismCategories'

interface TourismMapProps {
  apiKey: string
  mapId?: string
  language: Language
  attractions: PublicAttraction[]
  selectedAttraction: PublicAttraction | null
  focusAttraction: PublicAttraction | null
  focusZoom: number
  selectedRoute: PublicStarTourRoute | null
  routes: Array<{ route: PublicStarTourRoute; color: string }>
  visible: boolean
  detailsOpen: boolean
  showAttractionPopups: boolean
  onSelectAttraction: (attraction: PublicAttraction | null) => void
  onOpenAttractionDetails: (attraction: PublicAttraction) => void
}

const DEFAULT_CENTER = { lat: 45.75, lng: 23.12 }
const MOBILE_ATTRACTION_TRANSITION_DURATION = 800

export function getMapViewportLocations(
  selectedRoute: PublicStarTourRoute | null,
  attractions: PublicAttraction[],
) {
  if (selectedRoute) {
    return [
      { lat: selectedRoute.base.latitude, lng: selectedRoute.base.longitude },
      ...selectedRoute.stops.map((stop) => ({ lat: stop.latitude, lng: stop.longitude })),
    ]
  }

  return attractions.map((attraction) => ({ lat: attraction.latitude, lng: attraction.longitude }))
}

function decodePolyline(encoded: string): google.maps.LatLngLiteral[] {
  const path: google.maps.LatLngLiteral[] = []
  let index = 0
  let latitude = 0
  let longitude = 0

  while (index < encoded.length) {
    let shift = 0
    let result = 0
    let byte: number
    do {
      byte = encoded.charCodeAt(index++) - 63
      result |= (byte & 0x1f) << shift
      shift += 5
    } while (byte >= 0x20)
    latitude += result & 1 ? ~(result >> 1) : result >> 1

    shift = 0
    result = 0
    do {
      byte = encoded.charCodeAt(index++) - 63
      result |= (byte & 0x1f) << shift
      shift += 5
    } while (byte >= 0x20)
    longitude += result & 1 ? ~(result >> 1) : result >> 1

    path.push({ lat: latitude / 1e5, lng: longitude / 1e5 })
  }

  return path
}

function RoutePolylines({
  routes,
  selectedRoute,
}: Pick<TourismMapProps, 'routes' | 'selectedRoute'>) {
  const map = useMap()

  useEffect(() => {
    if (!map || routes.length === 0) return

    const polylines = routes.flatMap(({ route, color }) => {
      const selected = route.tourSlug === selectedRoute?.tourSlug
      return route.legs.flatMap((leg) => {
        const path = decodePolyline(leg.encodedPolyline)
        if (!selected) {
          return [
            new google.maps.Polyline({
              map,
              path,
              strokeColor: color,
              strokeOpacity: 0.55,
              strokeWeight: 4,
              clickable: false,
              zIndex: 10,
            }),
          ]
        }

        return [
          new google.maps.Polyline({
            map,
            path,
            strokeColor: '#fffaf0',
            strokeOpacity: 0.8,
            strokeWeight: 15,
            clickable: false,
            zIndex: 30,
          }),
          new google.maps.Polyline({
            map,
            path,
            strokeColor: color,
            strokeOpacity: 0.45,
            strokeWeight: 9,
            clickable: false,
            zIndex: 31,
          }),
          new google.maps.Polyline({
            map,
            path,
            strokeColor: color,
            strokeOpacity: 1,
            strokeWeight: 6,
            clickable: false,
            zIndex: 32,
          }),
        ]
      })
    })

    return () => polylines.forEach((polyline) => polyline.setMap(null))
  }, [map, routes, selectedRoute?.tourSlug])

  return null
}

function MapViewport({
  attractions,
  selectedRoute,
  visible,
}: Pick<TourismMapProps, 'attractions' | 'selectedRoute' | 'visible'>) {
  const map = useMap()
  const wasVisibleRef = useRef(false)

  useEffect(() => {
    if (!map) return

    const needsResize = visible && !wasVisibleRef.current
    wasVisibleRef.current = visible
    if (!visible) return

    const bounds = new google.maps.LatLngBounds()
    const locations = getMapViewportLocations(selectedRoute, attractions)
    if (locations.length === 0) return
    locations.forEach((location) => bounds.extend(location))

    const latitudes = locations.map((location) => location.lat)
    const longitudes = locations.map((location) => location.lng)
    const latitudeSpan = Math.max(...latitudes) - Math.min(...latitudes)
    const longitudeSpan = Math.max(...longitudes) - Math.min(...longitudes)

    if (latitudeSpan < 0.04 && longitudeSpan < 0.04) {
      const centerLatitude = (Math.max(...latitudes) + Math.min(...latitudes)) / 2
      const centerLongitude = (Math.max(...longitudes) + Math.min(...longitudes)) / 2
      const latitudePadding = 0.02
      const longitudePadding =
        latitudePadding / Math.max(Math.cos((centerLatitude * Math.PI) / 180), 0.2)
      bounds.extend({
        lat: centerLatitude - latitudePadding,
        lng: centerLongitude - longitudePadding,
      })
      bounds.extend({
        lat: centerLatitude + latitudePadding,
        lng: centerLongitude + longitudePadding,
      })
    }

    const animationFrame = window.requestAnimationFrame(() => {
      if (needsResize) google.maps.event.trigger(map, 'resize')
      map.fitBounds(bounds, 72)
    })

    return () => window.cancelAnimationFrame(animationFrame)
  }, [attractions, map, selectedRoute, visible])

  return null
}

function FocusedAttractionViewport({
  attraction,
  focusZoom,
  visible,
}: {
  attraction: PublicAttraction | null
  focusZoom: number
  visible: boolean
}) {
  const map = useMap()

  useEffect(() => {
    if (!map || !visible || !attraction) return

    let secondAnimationFrame: number | null = null
    let transitionAnimationFrame: number | null = null
    const firstAnimationFrame = window.requestAnimationFrame(() => {
      secondAnimationFrame = window.requestAnimationFrame(() => {
        const currentCenter = map.getCenter()
        const currentZoom = map.getZoom()
        if (!currentCenter || currentZoom === undefined) {
          map.moveCamera({
            center: { lat: attraction.latitude, lng: attraction.longitude },
            zoom: focusZoom,
          })
          return
        }

        const startLatitude = currentCenter.lat()
        const startLongitude = currentCenter.lng()
        const startTime = performance.now()
        const animate = (timestamp: number) => {
          const progress = Math.min(
            (timestamp - startTime) / MOBILE_ATTRACTION_TRANSITION_DURATION,
            1,
          )
          const easedProgress =
            progress < 0.5 ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2

          map.moveCamera({
            center: {
              lat: startLatitude + (attraction.latitude - startLatitude) * easedProgress,
              lng: startLongitude + (attraction.longitude - startLongitude) * easedProgress,
            },
            zoom: currentZoom + (focusZoom - currentZoom) * easedProgress,
          })

          if (progress < 1) transitionAnimationFrame = window.requestAnimationFrame(animate)
        }

        transitionAnimationFrame = window.requestAnimationFrame(animate)
      })
    })

    return () => {
      window.cancelAnimationFrame(firstAnimationFrame)
      if (secondAnimationFrame !== null) window.cancelAnimationFrame(secondAnimationFrame)
      if (transitionAnimationFrame !== null) {
        window.cancelAnimationFrame(transitionAnimationFrame)
      }
    }
  }, [attraction, focusZoom, map, visible])

  return null
}

function MapContent({
  attractions,
  selectedAttraction,
  focusAttraction,
  focusZoom,
  selectedRoute,
  routes,
  visible,
  detailsOpen,
  showAttractionPopups,
  onSelectAttraction,
  onOpenAttractionDetails,
}: Omit<TourismMapProps, 'apiKey' | 'mapId' | 'language'>) {
  const { t } = useTranslation()
  const [hoveredAttraction, setHoveredAttraction] = useState<PublicAttraction | null>(null)
  const hoverDismissTimeoutRef = useRef<number | null>(null)
  const routeStops = useMemo(
    () => new Set(selectedRoute?.stops.map((stop) => stop.slug) ?? []),
    [selectedRoute?.stops],
  )
  const guesthouseBase = selectedRoute?.base ?? routes[0]?.route.base
  const infoAttraction = showAttractionPopups ? (hoveredAttraction ?? selectedAttraction) : null

  function cancelHoverDismiss() {
    if (hoverDismissTimeoutRef.current === null) return
    window.clearTimeout(hoverDismissTimeoutRef.current)
    hoverDismissTimeoutRef.current = null
  }

  function showAttractionInfo(attraction: PublicAttraction) {
    cancelHoverDismiss()
    setHoveredAttraction(attraction)
  }

  function dismissAttractionInfoSoon() {
    cancelHoverDismiss()
    hoverDismissTimeoutRef.current = window.setTimeout(() => {
      setHoveredAttraction(null)
      hoverDismissTimeoutRef.current = null
    }, 160)
  }

  function selectAttraction(attraction: PublicAttraction | null) {
    setHoveredAttraction(null)
    onSelectAttraction(attraction)
  }

  function openAttractionDetails(attraction: PublicAttraction) {
    setHoveredAttraction(null)
    onOpenAttractionDetails(attraction)
  }

  useEffect(
    () => () => {
      cancelHoverDismiss()
    },
    [],
  )

  return (
    <>
      <MapViewport attractions={attractions} selectedRoute={selectedRoute} visible={visible} />
      <FocusedAttractionViewport
        attraction={focusAttraction}
        focusZoom={focusZoom}
        visible={visible}
      />
      <RoutePolylines routes={routes} selectedRoute={selectedRoute} />
      {guesthouseBase && (
        <AdvancedMarker
          position={{ lat: Number(guesthouseBase.latitude), lng: Number(guesthouseBase.longitude) }}
          title={t('tourism.guesthouseBase')}
          zIndex={40}
        >
          <span className="tourism-map-base-marker">
            <House aria-hidden="true" size={22} />
          </span>
        </AdvancedMarker>
      )}
      {attractions.map((attraction) => {
        const active =
          routeStops.has(attraction.slug) || selectedAttraction?.slug === attraction.slug
        const category = categoryForAttraction(attraction)
        return (
          <AdvancedMarker
            key={attraction.slug}
            position={{ lat: attraction.latitude, lng: attraction.longitude }}
            title={attraction.name}
            zIndex={active ? 30 : 20}
            onClick={() => selectAttraction(attraction)}
            onMouseEnter={showAttractionPopups ? () => showAttractionInfo(attraction) : undefined}
            onMouseLeave={showAttractionPopups ? dismissAttractionInfoSoon : undefined}
          >
            <span
              className={`tourism-map-marker${active ? ' tourism-map-marker-active' : ''}`}
              style={
                active
                  ? ({
                      '--marker-accent':
                        routes.find(({ route }) => route.tourSlug === selectedRoute?.tourSlug)
                          ?.color ?? '#a84930',
                    } as CSSProperties)
                  : undefined
              }
            >
              <AttractionCategoryIcon category={category} />
            </span>
          </AdvancedMarker>
        )
      })}
      {!detailsOpen && infoAttraction && (
        <InfoWindow
          position={{
            lat: infoAttraction.latitude,
            lng: infoAttraction.longitude,
          }}
          pixelOffset={[0, -42]}
          headerContent={
            <span className="tourism-map-popup-category">
              <AttractionCategoryIcon category={categoryForAttraction(infoAttraction)} size={15} />
              {infoAttraction.name}
            </span>
          }
          onCloseClick={() => {
            selectAttraction(null)
          }}
        >
          <div
            className="tourism-map-popup"
            onMouseEnter={cancelHoverDismiss}
            onMouseLeave={dismissAttractionInfoSoon}
          >
            <p>{infoAttraction.shortDescription}</p>
            <div className="tourism-map-popup-actions">
              <a href={infoAttraction.googleMapsUrl} target="_blank" rel="noreferrer">
                {t('tourism.openOnMap')}
              </a>
              <button type="button" onClick={() => openAttractionDetails(infoAttraction)}>
                {t('tourism.details')}
              </button>
            </div>
          </div>
        </InfoWindow>
      )}
    </>
  )
}

export default function TourismMap({ apiKey, mapId, language, ...props }: TourismMapProps) {
  return (
    <APIProvider apiKey={apiKey} language={language} region="RO">
      <Map
        className="tourism-google-map"
        defaultCenter={DEFAULT_CENTER}
        defaultZoom={8}
        mapId={mapId || 'DEMO_MAP_ID'}
        colorScheme="LIGHT"
        renderingType="VECTOR"
        gestureHandling="greedy"
        keyboardShortcuts={false}
        disableDefaultUI
        zoomControl={false}
        clickableIcons={false}
        reuseMaps
      >
        <MapContent {...props} />
      </Map>
    </APIProvider>
  )
}
