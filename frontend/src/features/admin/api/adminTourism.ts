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
}

export type StarTourUpdate = Omit<AdminStarTour, 'id'>

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
) =>
  request<AdminAttraction>(
    authorizedFetch,
    id ? `/api/admin/tourism/attractions/${id}` : '/api/admin/tourism/attractions',
    json(id ? 'PUT' : 'POST', attraction),
  )

export const fetchStarTours = (authorizedFetch: AuthorizedFetch, signal?: AbortSignal) =>
  request<AdminStarTour[]>(authorizedFetch, '/api/admin/tourism/star-tours', { signal })

export const saveStarTour = (authorizedFetch: AuthorizedFetch, tour: StarTourUpdate, id?: string) =>
  request<AdminStarTour>(
    authorizedFetch,
    id ? `/api/admin/tourism/star-tours/${id}` : '/api/admin/tourism/star-tours',
    json(id ? 'PUT' : 'POST', tour),
  )
