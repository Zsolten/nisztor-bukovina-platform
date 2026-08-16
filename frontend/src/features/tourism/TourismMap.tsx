import { AdvancedMarker, APIProvider, InfoWindow, Map, useMap } from '@vis.gl/react-google-maps'
import { useEffect, useMemo, type CSSProperties } from 'react'
import type { Language } from '../../i18n/languages'
import type {
  PublicAttraction,
  PublicStarTourRoute,
  StarTourRouteLeg,
} from '../../shared/api/tourism'
import AttractionCategoryIcon from './AttractionCategoryIcon'
import { categoryForAttraction } from './tourismCategories'

interface TourismMapProps {
  apiKey: string
  mapId?: string
  language: Language
  attractions: PublicAttraction[]
  selectedAttraction: PublicAttraction | null
  route: PublicStarTourRoute | null
  routeColor: string
  onSelectAttraction: (attraction: PublicAttraction | null) => void
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

function RoutePolylines({ legs, color }: { legs: StarTourRouteLeg[]; color: string }) {
  const map = useMap()

  useEffect(() => {
    if (!map || legs.length === 0) return

    const polylines = legs.map(
      (leg) =>
        new google.maps.Polyline({
          map,
          path: decodePolyline(leg.encodedPolyline),
          strokeColor: color,
          strokeOpacity: 0.95,
          strokeWeight: 5,
          clickable: false,
          zIndex: 10,
        }),
    )

    return () => polylines.forEach((polyline) => polyline.setMap(null))
  }, [color, legs, map])

  return null
}

function MapViewport({ attractions, route }: Pick<TourismMapProps, 'attractions' | 'route'>) {
  const map = useMap()

  useEffect(() => {
    if (!map || attractions.length === 0) return

    const routeSlugs = new Set(route?.stops.map((stop) => stop.slug) ?? [])
    const visibleAttractions =
      routeSlugs.size > 0
        ? attractions.filter((attraction) => routeSlugs.has(attraction.slug))
        : attractions
    const bounds = new google.maps.LatLngBounds()
    visibleAttractions.forEach((attraction) =>
      bounds.extend({ lat: attraction.latitude, lng: attraction.longitude }),
    )
    if (route) bounds.extend({ lat: route.base.latitude, lng: route.base.longitude })
    map.fitBounds(bounds, 72)
  }, [attractions, map, route])

  return null
}

function MapContent({
  attractions,
  selectedAttraction,
  route,
  routeColor,
  onSelectAttraction,
}: Omit<TourismMapProps, 'apiKey' | 'mapId' | 'language'>) {
  const routeStops = useMemo(
    () => new Set(route?.stops.map((stop) => stop.slug) ?? []),
    [route?.stops],
  )

  return (
    <>
      <MapViewport attractions={attractions} route={route} />
      {route?.routeStatus === 'READY' && <RoutePolylines legs={route.legs} color={routeColor} />}
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
              style={active ? ({ '--marker-accent': routeColor } as CSSProperties) : undefined}
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
            <a href={selectedAttraction.googleMapsUrl} target="_blank" rel="noreferrer">
              Megnyitás Google Térképen
            </a>
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
