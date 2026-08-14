export type RoomTypeLanguage = 'hu' | 'ro' | 'en'

export interface AdminRoomTypeTranslation {
  language: RoomTypeLanguage
  name: string
  shortDescription: string
  detailedDescription: string
}

export interface AdminRoomType {
  id: string
  code: string
  quantity: number
  standardOccupancy: number
  roomsWithExtraBed: number
  extraBedsPerEligibleRoom: number
  active: boolean
  displayOrder: number
  translations: AdminRoomTypeTranslation[]
}

export type AdminRoomTypeUpdate = Omit<AdminRoomType, 'id' | 'displayOrder'>

export class AdminRoomTypeApiError extends Error {
  constructor(public readonly code: string) {
    super(code)
  }
}

async function request<T>(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  path: string,
  init?: RequestInit,
): Promise<T> {
  const response = await authorizedFetch(path, init)
  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as { code?: string }
    throw new AdminRoomTypeApiError(body.code ?? 'ADMIN_ROOM_TYPE_REQUEST_FAILED')
  }
  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

export function fetchAdminRoomTypes(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  guesthouseId: string,
  signal?: AbortSignal,
) {
  return request<AdminRoomType[]>(
    authorizedFetch,
    `/api/admin/guesthouses/${guesthouseId}/room-types`,
    {
      signal,
    },
  )
}

export function createAdminRoomType(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  guesthouseId: string,
  payload: AdminRoomTypeUpdate,
) {
  return request<AdminRoomType>(
    authorizedFetch,
    `/api/admin/guesthouses/${guesthouseId}/room-types`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    },
  )
}

export function updateAdminRoomType(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  guesthouseId: string,
  roomTypeId: string,
  payload: AdminRoomTypeUpdate,
) {
  return request<AdminRoomType>(
    authorizedFetch,
    `/api/admin/guesthouses/${guesthouseId}/room-types/${roomTypeId}`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    },
  )
}

export function reorderAdminRoomTypes(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  guesthouseId: string,
  roomTypeIds: string[],
) {
  return request<void>(authorizedFetch, `/api/admin/guesthouses/${guesthouseId}/room-types/order`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ roomTypeIds }),
  })
}
