import { describe, expect, it } from 'vitest'
import type { PublicAttraction, PublicStarTourRoute } from '../../shared/api/tourism'
import { getMapViewportLocations } from './TourismMap'

const attraction: PublicAttraction = {
  slug: 'deva-vara',
  name: 'Déva vára',
  shortDescription: 'Középkori várrom.',
  detailedDescription: 'Részletes leírás.',
  admissionInformation: null,
  practicalInformation: null,
  latitude: 45.8763,
  longitude: 22.9008,
  googleMapsUrl: 'https://maps.google.com/?q=45.8763,22.9008',
  recommendedVisitDurationMinutes: 90,
  collectionSlugs: [],
}

const route: PublicStarTourRoute = {
  tourSlug: 'paring-es-hatszegi-medence',
  routeStatus: 'READY',
  cached: true,
  base: { latitude: 45.8232811, longitude: 22.930933 },
  stops: [
    {
      waypointIndex: 1,
      slug: 'paring-hegyseg',
      latitude: 45.3935,
      longitude: 23.4461,
      optional: false,
    },
  ],
  legs: [],
  totalDistanceMeters: 158000,
  totalDurationSeconds: 31440,
  failureReason: null,
  retryAfter: null,
}

describe('getMapViewportLocations', () => {
  it('focuses the selected tour base and stops instead of every attraction', () => {
    expect(getMapViewportLocations(route, [attraction])).toEqual([
      { lat: 45.8232811, lng: 22.930933 },
      { lat: 45.3935, lng: 23.4461 },
    ])
  })

  it('falls back to all attractions when no tour route is selected', () => {
    expect(getMapViewportLocations(null, [attraction])).toEqual([{ lat: 45.8763, lng: 22.9008 }])
  })
})
