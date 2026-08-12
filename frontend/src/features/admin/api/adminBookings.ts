export type AdminBookingStatus =
  'RECEIVED' | 'UNDER_REVIEW' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED'

export interface AdminBookingSummary {
  id: string
  publicReference: string
  guesthouseId: string
  guesthouseName: string
  status: AdminBookingStatus
  checkInDate: string
  checkOutDate: string
  nights: number
  totalGuests: number
  contactName: string
  totalPayable: number
  currency: string
  createdAt: string
}

export interface AdminBookingPage {
  content: AdminBookingSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface AdminBookingDetail {
  id: string
  publicReference: string
  guesthouse: { id: string; name: string }
  stay: {
    checkInDate: string
    checkOutDate: string
    nights: number
    adults: number
    childrenAge3to10: number
    childrenAge0to3: number
  }
  contact: { name: string; email: string; phone: string; preferredLanguage: 'hu' | 'ro' | 'en' }
  services: { breakfastParticipants: number; dinnerParticipants: number }
  rooms: Array<{ roomTypeId: string; roomTypeName: string; quantity: number }>
  priceSnapshot: {
    accommodationTotal: number
    adultAccommodationTotal: number
    childAccommodationTotal: number
    singleRoomSurcharge: number
    breakfastTotal: number
    dinnerTotal: number
    totalPayable: number
    currency: string
  }
  status: AdminBookingStatus
  statusHistory: Array<{
    status: AdminBookingStatus
    changedAt: string
    changedBy: string
  }>
  guestNote: string | null
  internalNote: string | null
  createdAt: string
  updatedAt: string
}

export class AdminBookingApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code?: string,
  ) {
    super('Admin booking operation failed')
  }
}

export interface AdminBookingFilters {
  guesthouseId: string
  search: string
  status: AdminBookingStatus | ''
}

export type AdminBookingSortField = 'checkInDate' | 'totalPayable' | 'createdAt'
export type AdminBookingSortDirection = 'asc' | 'desc'

export async function fetchAdminBookings(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  filters: AdminBookingFilters,
  page: number,
  sortBy: AdminBookingSortField,
  sortDirection: AdminBookingSortDirection,
  signal?: AbortSignal,
) {
  const parameters = new URLSearchParams({
    page: String(page),
    size: '20',
    sortBy,
    sortDirection,
  })
  if (filters.guesthouseId) parameters.set('guesthouseId', filters.guesthouseId)
  if (filters.search) parameters.set('search', filters.search)
  if (filters.status) parameters.set('status', filters.status)

  const response = await authorizedFetch(`/api/admin/bookings?${parameters}`, {
    headers: { Accept: 'application/json' },
    signal,
  })

  if (!response.ok) {
    throw new Error(`Admin booking request failed with status ${response.status}`)
  }

  const body = (await response.json()) as AdminBookingPage
  if (!Array.isArray(body.content)) {
    throw new Error('Invalid admin booking response')
  }
  return body
}

export async function fetchAdminBookingDetail(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  bookingId: string,
  signal?: AbortSignal,
) {
  const response = await authorizedFetch(`/api/admin/bookings/${bookingId}`, {
    headers: { Accept: 'application/json' },
    signal,
  })
  if (!response.ok) throw await adminBookingError(response)
  return (await response.json()) as AdminBookingDetail
}

export async function updateAdminBookingStatus(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  bookingId: string,
  status: AdminBookingStatus,
) {
  await patchAdminBooking(authorizedFetch, `/api/admin/bookings/${bookingId}/status`, { status })
}

export async function updateAdminBookingInternalNote(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  bookingId: string,
  internalNote: string,
) {
  await patchAdminBooking(authorizedFetch, `/api/admin/bookings/${bookingId}/internal-note`, {
    internalNote,
  })
}

async function patchAdminBooking(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  url: string,
  body: object,
) {
  const response = await authorizedFetch(url, {
    method: 'PATCH',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!response.ok) throw await adminBookingError(response)
}

async function adminBookingError(response: Response) {
  let code: string | undefined
  try {
    const body = (await response.json()) as { code?: string }
    code = body.code
  } catch {
    // The UI maps known codes and uses a generic localized fallback otherwise.
  }
  return new AdminBookingApiError(response.status, code)
}
