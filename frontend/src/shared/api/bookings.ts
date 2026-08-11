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
    adultAccommodationTotal: number
    childAccommodationTotal: number
    singleRoomSurcharge: number
    breakfastTotal: number
    dinnerTotal: number
    totalPayable: number
  }
  requestOnly: true
}

export interface BookingContactDetails {
  contactName: string
  contactEmail: string
  contactPhone: string
  preferredLanguage: 'hu' | 'ro' | 'en' | ''
  note: string
}

export interface CreateBookingRequest extends BookingQuoteRequest, BookingContactDetails {
  acceptedTotal: number
}

export interface BookingFieldError {
  code: string
  field: string
  rule: string
}

export interface BookingApiErrorBody {
  code: string
  errors: BookingFieldError[]
  currentQuote: BookingQuote | null
}

export class BookingApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly body: BookingApiErrorBody,
  ) {
    super(`Booking API request failed with status ${status}`)
  }
}

export interface BookingRequestCreated {
  reference: string
  status: 'RECEIVED'
  currency: 'RON'
  nights: number
  totalGuests: number
  totalPayable: number
  requestOnly: true
}

async function bookingApiError(response: Response): Promise<BookingApiError> {
  try {
    const body = (await response.json()) as Partial<BookingApiErrorBody>
    return new BookingApiError(response.status, {
      code: body.code ?? 'UNEXPECTED_BOOKING_ERROR',
      errors: Array.isArray(body.errors) ? body.errors : [],
      currentQuote: body.currentQuote ?? null,
    })
  } catch {
    return new BookingApiError(response.status, {
      code: 'UNEXPECTED_BOOKING_ERROR',
      errors: [],
      currentQuote: null,
    })
  }
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
    throw await bookingApiError(response)
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

export function submitBookingRequest(
  request: CreateBookingRequest,
  idempotencyKey: string,
  signal?: AbortSignal,
) {
  return getJson<BookingRequestCreated>('/api/booking-requests', {
    method: 'POST',
    body: JSON.stringify(request),
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey,
    },
    signal,
  })
}
