export interface MapCoordinates {
  latitude: number
  longitude: number
}

function coordinateValue({ latitude, longitude }: MapCoordinates) {
  return `${latitude},${longitude}`
}

function directionUrl(
  origin: MapCoordinates,
  destination: MapCoordinates,
  waypoints: MapCoordinates[] = [],
) {
  const parameters = new URLSearchParams({
    api: '1',
    origin: coordinateValue(origin),
    destination: coordinateValue(destination),
    travelmode: 'driving',
  })

  if (waypoints.length > 0) {
    parameters.set('waypoints', waypoints.map(coordinateValue).join('|'))
  }

  return `https://www.google.com/maps/dir/?${parameters.toString()}`
}

/** Creates one driving route from the guesthouse, through every stop, and back. */
export function buildGoogleMapsDirectionsUrl(base: MapCoordinates, stops: MapCoordinates[]) {
  if (stops.length === 0) return null
  return directionUrl(base, base, stops)
}
