import type { Language } from '../../i18n/languages'

export type StarTourRouteStatus = 'READY' | 'MISSING' | 'STALE' | 'CALCULATING' | 'FAILED'

export interface TourismImage {
  imageUrl: string
  altText: string
}

export interface StarTourStop {
  slug: string
  name: string
  latitude: number
  longitude: number
  googleMapsUrl: string
  optional: boolean
  visitDurationMinutes: number
}

export interface StarTourTotals {
  travelDistanceMeters: number | null
  travelDurationSeconds: number | null
  visitDurationMinutes: number
  totalDurationSeconds: number | null
  routeDataComplete: boolean
}

export interface PublicStarTour {
  slug: string
  name: string
  shortDescription: string
  detailedDescription: string
  mapColor: string
  tags: string[]
  images: TourismImage[]
  stops: StarTourStop[]
  totals: StarTourTotals
  routeStatus: StarTourRouteStatus
}

export interface PublicAttraction {
  slug: string
  name: string
  shortDescription: string
  detailedDescription: string
  admissionInformation: string | null
  practicalInformation: string | null
  latitude: number
  longitude: number
  googleMapsUrl: string
  recommendedVisitDurationMinutes: number
  collectionSlugs: string[]
}

export interface StarTourRouteLeg {
  order: number
  fromStopIndex: number
  toStopIndex: number
  distanceMeters: number
  durationSeconds: number
  encodedPolyline: string
}

export interface PublicStarTourRoute {
  tourSlug: string
  routeStatus: StarTourRouteStatus
  cached: boolean
  base: { latitude: number; longitude: number }
  stops: Array<{
    waypointIndex: number
    slug: string
    latitude: number
    longitude: number
    optional: boolean
  }>
  legs: StarTourRouteLeg[]
  totalDistanceMeters: number
  totalDurationSeconds: number
  failureReason: string | null
  retryAfter: string | null
}

async function request<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(path, {
    headers: { Accept: 'application/json' },
    signal,
  })

  if (!response.ok) {
    throw new Error(`TOURISM_REQUEST_FAILED_${response.status}`)
  }

  return response.json() as Promise<T>
}

export function listPublicStarTours(language: Language, signal?: AbortSignal) {
  return request<PublicStarTour[]>(
    `/api/tourism/star-tours?lang=${encodeURIComponent(language)}`,
    signal,
  )
}

export function listPublicAttractions(language: Language, signal?: AbortSignal) {
  return request<PublicAttraction[]>(
    `/api/tourism/attractions?lang=${encodeURIComponent(language)}`,
    signal,
  )
}

export function getPublicStarTourRoute(slug: string, signal?: AbortSignal) {
  return request<PublicStarTourRoute>(
    `/api/tourism/star-tours/${encodeURIComponent(slug)}/route`,
    signal,
  )
}
