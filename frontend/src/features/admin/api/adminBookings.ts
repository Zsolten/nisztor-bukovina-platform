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

export interface AdminBookingFilters {
  guesthouseId: string
  status: AdminBookingStatus | ''
}

export async function fetchAdminBookings(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  filters: AdminBookingFilters,
  page: number,
  signal?: AbortSignal,
) {
  const parameters = new URLSearchParams({ page: String(page), size: '20' })
  if (filters.guesthouseId) parameters.set('guesthouseId', filters.guesthouseId)
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
