import { afterEach, describe, expect, it, vi } from 'vitest'
import { submitBookingRequest } from './bookings'

const request = {
  guesthouseId: '10000000-0000-0000-0000-000000000001',
  checkInDate: '2026-09-10',
  checkOutDate: '2026-09-12',
  adults: 2,
  childrenAge3to10: 0,
  childrenAge0to3: 0,
  roomSelections: [{ roomTypeId: '20000000-0000-0000-0000-000000000001', quantity: 1 }],
  services: { breakfastParticipants: 0, dinnerParticipants: 0 },
  contactName: 'Test Guest',
  contactEmail: 'guest@example.com',
  contactPhone: '+40743677812',
  preferredLanguage: 'hu' as const,
  note: '',
  acceptedTotal: 520,
}

afterEach(() => vi.unstubAllGlobals())

describe('booking request API', () => {
  it('sends the idempotency key with the final booking request', async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        reference: 'NB-0123456789ABCDEF',
        status: 'RECEIVED',
        currency: 'RON',
        nights: 2,
        totalGuests: 2,
        totalPayable: 520,
        requestOnly: true,
      }),
    })
    vi.stubGlobal('fetch', fetchMock)

    await submitBookingRequest(request, 'booking-attempt-1')

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/booking-requests',
      expect.objectContaining({
        method: 'POST',
        headers: expect.objectContaining({ 'Idempotency-Key': 'booking-attempt-1' }),
      }),
    )
  })

  it('preserves machine-readable price-change responses for the review step', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 409,
        json: async () => ({
          code: 'BOOKING_PRICE_CHANGED',
          errors: [],
          currentQuote: {
            currency: 'RON',
            priceBreakdown: {
              accommodationTotal: 650,
              singleRoomSurcharge: 0,
              breakfastTotal: 0,
              dinnerTotal: 0,
              totalPayable: 650,
            },
          },
        }),
      }),
    )

    await expect(submitBookingRequest(request, 'booking-attempt-1')).rejects.toMatchObject({
      status: 409,
      body: {
        code: 'BOOKING_PRICE_CHANGED',
        currentQuote: { priceBreakdown: { totalPayable: 650 } },
      },
    })
  })
})
