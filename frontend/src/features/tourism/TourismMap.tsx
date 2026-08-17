import { AdvancedMarker, APIProvider, InfoWindow, Map, useMap } from '@vis.gl/react-google-maps'
import { House } from 'lucide-react'
import { useEffect, useMemo, useRef, type CSSProperties } from 'react'
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
  selectedRoute: PublicStarTourRoute | null
  routes: Array<{ route: PublicStarTourRoute; color: string }>
  visible: boolean
  onSelectAttraction: (attraction: PublicAttraction | null) => void
  onOpenAttractionDetails: (attraction: PublicAttraction) => void
}

const DEFAULT_CENTER = { lat: 45.75, lng: 23.12 }

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
    if (!visible || attractions.length === 0) return

    const bounds = new google.maps.LatLngBounds()
    const locations = attractions.map((attraction) => ({
      lat: attraction.latitude,
      lng: attraction.longitude,
    }))
    if (selectedRoute) {
      locations.push({ lat: selectedRoute.base.latitude, lng: selectedRoute.base.longitude })
    }
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

function MapContent({
  attractions,
  selectedAttraction,
  selectedRoute,
  routes,
  visible,
  onSelectAttraction,
  onOpenAttractionDetails,
}: Omit<TourismMapProps, 'apiKey' | 'mapId' | 'language'>) {
  const { t } = useTranslation()
  const routeStops = useMemo(
    () => new Set(selectedRoute?.stops.map((stop) => stop.slug) ?? []),
    [selectedRoute?.stops],
  )
  const guesthouseBase = selectedRoute?.base ?? routes[0]?.route.base

  return (
    <>
      <MapViewport attractions={attractions} selectedRoute={selectedRoute} visible={visible} />
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
        const active = routeStops.has(attraction.slug)
        const category = categoryForAttraction(attraction)
        return (
          <AdvancedMarker
            key={attraction.slug}
            position={{ lat: attraction.latitude, lng: attraction.longitude }}
            title={attraction.name}
            zIndex={active ? 30 : 20}
            onClick={() => onSelectAttraction(attraction)}
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
      {selectedAttraction && (
        <InfoWindow
          position={{
            lat: selectedAttraction.latitude,
            lng: selectedAttraction.longitude,
          }}
          pixelOffset={[0, -42]}
          headerContent={
            <span className="tourism-map-popup-category">
              <AttractionCategoryIcon
                category={categoryForAttraction(selectedAttraction)}
                size={15}
              />
              {selectedAttraction.name}
            </span>
          }
          onCloseClick={() => onSelectAttraction(null)}
        >
          <div className="tourism-map-popup">
            <p>{selectedAttraction.shortDescription}</p>
            <div className="tourism-map-popup-actions">
              <a href={selectedAttraction.googleMapsUrl} target="_blank" rel="noreferrer">
                {t('tourism.openOnMap')}
              </a>
              <button type="button" onClick={() => onOpenAttractionDetails(selectedAttraction)}>
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
        zoomControl
        clickableIcons={false}
        reuseMaps
      >
        <MapContent {...props} />
      </Map>
    </APIProvider>
  )
}
