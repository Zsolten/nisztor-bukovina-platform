export type TourismLanguage = 'hu' | 'ro' | 'en'

type AuthorizedFetch = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>

export interface AttractionTranslation {
  language: TourismLanguage
  name: string
  shortDescription: string
  detailedDescription: string
  admissionInformation: string
  practicalInformation: string
}

export interface AdminAttraction {
  id: string
  slug: string
  latitude: number
  longitude: number
  googleMapsUrl: string
  recommendedVisitDurationMinutes: number
  active: boolean
  translations: AttractionTranslation[]
  collectionSlugs: string[]
  distanceCalculation?: DistanceCalculation | null
}

export interface DistanceCalculation {
  total: number
  successful: number
  failed: number
}

export type AttractionUpdate = Omit<AdminAttraction, 'id' | 'distanceCalculation'>

export interface StarTourTranslation {
  language: TourismLanguage
  name: string
  shortDescription: string
  detailedDescription: string
}

export interface StarTourImage {
  imageUrl: string
  altText: string
}

export interface AdminStarTour {
  id: string
  slug: string
  mapColor: string
  published: boolean
  active: boolean
  translations: StarTourTranslation[]
  tags: string[]
  images: StarTourImage[]
  routeStatus: StarTourRouteStatus
  routeFailureReason?: string | null
}

export type StarTourRouteStatus = 'READY' | 'MISSING' | 'STALE' | 'CALCULATING' | 'FAILED'

export type StarTourUpdate = Omit<AdminStarTour, 'id' | 'routeStatus' | 'routeFailureReason'>

async function request<T>(authorizedFetch: AuthorizedFetch, path: string, init?: RequestInit) {
  const response = await authorizedFetch(path, init)
  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as { detail?: string; code?: string }
    throw new Error(body.code ?? body.detail ?? 'TOURISM_REQUEST_FAILED')
  }
  return (await response.json()) as T
}

const json = (method: 'POST' | 'PUT', body: unknown): RequestInit => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
})

export const fetchAttractions = (authorizedFetch: AuthorizedFetch, signal?: AbortSignal) =>
  request<AdminAttraction[]>(authorizedFetch, '/api/admin/tourism/attractions', { signal })

export const saveAttraction = (
  authorizedFetch: AuthorizedFetch,
  attraction: AttractionUpdate,
  id?: string,
) => {
  const payload: AttractionUpdate = {
    slug: attraction.slug,
    latitude: attraction.latitude,
    longitude: attraction.longitude,
    googleMapsUrl: attraction.googleMapsUrl,
    recommendedVisitDurationMinutes: attraction.recommendedVisitDurationMinutes,
    active: attraction.active,
    translations: attraction.translations,
    collectionSlugs: attraction.collectionSlugs,
  }
  return request<AdminAttraction>(
    authorizedFetch,
    id ? `/api/admin/tourism/attractions/${id}` : '/api/admin/tourism/attractions',
    json(id ? 'PUT' : 'POST', payload),
  )
}

export const fetchStarTours = (authorizedFetch: AuthorizedFetch, signal?: AbortSignal) =>
  request<AdminStarTour[]>(authorizedFetch, '/api/admin/tourism/star-tours', { signal })

export const saveStarTour = (
  authorizedFetch: AuthorizedFetch,
  tour: StarTourUpdate,
  id?: string,
) => {
  const payload: StarTourUpdate = {
    slug: tour.slug,
    mapColor: tour.mapColor,
    published: tour.published,
    active: tour.active,
    translations: tour.translations,
    tags: tour.tags,
    images: tour.images,
  }
  return request<AdminStarTour>(
    authorizedFetch,
    id ? `/api/admin/tourism/star-tours/${id}` : '/api/admin/tourism/star-tours',
    json(id ? 'PUT' : 'POST', payload),
  )
}

export const recalculateStarTourRoute = (authorizedFetch: AuthorizedFetch, id: string) =>
  request<AdminStarTour>(
    authorizedFetch,
    `/api/admin/tourism/star-tours/${id}/route/recalculate`,
    json('POST', undefined),
  )
