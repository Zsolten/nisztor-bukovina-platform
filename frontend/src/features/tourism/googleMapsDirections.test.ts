import { describe, expect, it } from 'vitest'
import { buildGoogleMapsDirectionsUrl } from './googleMapsDirections'

const base = { latitude: 45.8232811, longitude: 22.930933 }
const stops = [
  { latitude: 45.9, longitude: 22.8 },
  { latitude: 46.0, longitude: 22.7 },
  { latitude: 46.1, longitude: 22.6 },
  { latitude: 46.2, longitude: 22.5 },
]

describe('buildGoogleMapsDirectionsUrl', () => {
  it('creates one round-trip link that contains every tour stop', () => {
    const directionsUrl = buildGoogleMapsDirectionsUrl(base, stops)
    expect(directionsUrl).not.toBeNull()
    const url = new URL(directionsUrl!)

    expect(url.searchParams.get('api')).toBe('1')
    expect(url.searchParams.get('origin')).toBe('45.8232811,22.930933')
    expect(url.searchParams.get('destination')).toBe('45.8232811,22.930933')
    expect(url.searchParams.get('waypoints')).toBe('45.9,22.8|46,22.7|46.1,22.6|46.2,22.5')
    expect(url.searchParams.get('travelmode')).toBe('driving')
  })
})
