export interface BookingRoomSelection {
  roomTypeId: string
  quantity: number
}

export interface BookingQuoteRequest {
  guesthouseId: string
  checkInDate: string
  checkOutDate: string
  adults: number
  childrenAge3to10: number
  childrenAge0to3: number
  roomSelections: BookingRoomSelection[]
  services: {
    breakfastParticipants: number
    dinnerParticipants: number
  }
}

export interface BookingQuote {
  currency: 'RON'
  nights: number
  totalGuests: number
  selectedRoomCount: number
  selectedCapacity: number
  lines: Array<{
    code: string
    quantity: number
    unitAmount: number
    lineTotal: number
  }>
  priceBreakdown: {
    accommodationTotal: number
    singleRoomSurcharge: number
    breakfastTotal: number
    dinnerTotal: number
    totalPayable: number
  }
  requestOnly: true
}

async function getJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...init?.headers,
    },
  })

  if (!response.ok) {
    throw new Error(`Booking API request failed with status ${response.status}`)
  }

  return (await response.json()) as T
}

export function fetchBookingQuote(request: BookingQuoteRequest, signal?: AbortSignal) {
  return getJson<BookingQuote>('/api/booking-quotes', {
    method: 'POST',
    body: JSON.stringify(request),
    headers: { 'Content-Type': 'application/json' },
    signal,
  })
}
