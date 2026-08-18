export type AmenityCategory = 'ROOM_COMFORT' | 'FOOD_KITCHEN' | 'OUTDOOR_WELLNESS' | 'PROGRAM_GROUP'

export type AmenityPricingType = 'FREE' | 'PAID'
export type AmenityLanguage = 'hu' | 'ro' | 'en'

export interface AdminAmenityTranslation {
  language: AmenityLanguage
  name: string
  description: string
  detailedDescription: string
}

export interface AdminAmenityAssignment {
  guesthouseId: string
  active: boolean
  displayOrder: number
}

export interface AdminAmenity {
  id: string
  code: string
  category: AmenityCategory
  pricingType: AmenityPricingType
  translations: AdminAmenityTranslation[]
  assignments: AdminAmenityAssignment[]
}

export type AdminAmenityUpdate = Omit<AdminAmenity, 'id'>

export class AdminAmenityApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code?: string,
    public readonly fieldErrors: Record<string, string> = {},
  ) {
    super('Admin amenity operation failed')
  }
}

type AuthorizedFetch = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>

export async function fetchAdminAmenities(authorizedFetch: AuthorizedFetch, signal?: AbortSignal) {
  const response = await authorizedFetch('/api/admin/amenities', {
    headers: { Accept: 'application/json' },
    signal,
  })
  if (!response.ok) throw await amenityApiError(response)
  return (await response.json()) as AdminAmenity[]
}

export async function createAdminAmenity(
  authorizedFetch: AuthorizedFetch,
  amenity: AdminAmenityUpdate,
) {
  const response = await authorizedFetch('/api/admin/amenities', jsonRequest('POST', amenity))
  if (!response.ok) throw await amenityApiError(response)
  return (await response.json()) as AdminAmenity
}

export async function updateAdminAmenity(
  authorizedFetch: AuthorizedFetch,
  amenityId: string,
  amenity: AdminAmenityUpdate,
) {
  const response = await authorizedFetch(
    `/api/admin/amenities/${encodeURIComponent(amenityId)}`,
    jsonRequest('PUT', amenity),
  )
  if (!response.ok) throw await amenityApiError(response)
  return (await response.json()) as AdminAmenity
}

export async function reorderAdminAmenities(
  authorizedFetch: AuthorizedFetch,
  guesthouseId: string,
  amenityIds: string[],
) {
  const response = await authorizedFetch(
    `/api/admin/guesthouses/${encodeURIComponent(guesthouseId)}/amenities/order`,
    jsonRequest('PUT', { amenityIds }),
  )
  if (!response.ok) throw await amenityApiError(response)
}

function jsonRequest(method: 'POST' | 'PUT', body: unknown): RequestInit {
  return {
    method,
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }
}

async function amenityApiError(response: Response) {
  try {
    const body = (await response.json()) as { code?: string; fieldErrors?: Record<string, string> }
    return new AdminAmenityApiError(response.status, body.code, body.fieldErrors)
  } catch {
    return new AdminAmenityApiError(response.status)
  }
}
