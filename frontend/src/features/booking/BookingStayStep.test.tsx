import { render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'
import BookingStayStep from './BookingStayStep'
import { initialBookingFlowState } from './bookingReducer'
import { addDays, todayIso } from './bookingRules'

const guesthouse = {
  id: '10000000-0000-0000-0000-000000000001',
  slug: 'nisztor-panzio',
  name: 'Nisztor Panzió',
  shortDescription: 'Csendes szállás.',
  roomCount: 5,
  coverImage: { path: '/cover.jpg', altText: 'Nisztor Panzió', cover: true },
  description: 'Leírás',
  roomDescription: 'Szobák',
  pageText: {
    storyEyebrow: '',
    storyTitle: '',
    diningEyebrow: '',
    diningTitle: '',
    diningDescription: '',
    amenitiesTitle: '',
    roomTypesTitle: '',
    pricingTitle: '',
    historyEyebrow: '',
    galleryTitle: '',
    galleryHint: '',
  },
  images: [],
  history: { title: 'Történet', text: 'Szöveg' },
  contacts: [],
  address: { formatted: 'Csernakeresztúr', latitude: 45, longitude: 22 },
  roomTypes: [
    {
      id: 'double',
      name: 'Kétágyas szoba',
      quantity: 3,
      standardOccupancy: 2,
      roomsWithExtraBed: 0,
      extraBedsPerEligibleRoom: 0,
      features: [],
    },
  ],
  amenities: [],
  pricing: {
    currency: 'RON',
    items: [{ id: 'lodging', label: 'Szállás', amount: 130, unit: 'person_night' }],
    taxes: [{ id: 'city-tax', label: 'Idegenforgalmi adó', percentage: 1 }],
    surcharges: [],
    discounts: [],
    paymentNote: 'Fizetés érkezéskor.',
  },
} satisfies GuesthouseDetail

function okJson(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as Response
}

function renderStep(adults: number, roomQuantity: number) {
  const checkInDate = addDays(todayIso(), 2)
  const checkOutDate = addDays(checkInDate, 3)

  render(
    <AppProviders>
      <BookingStayStep
        language="hu"
        guesthouse={guesthouse}
        state={{
          ...initialBookingFlowState,
          guesthouseId: guesthouse.id,
          guesthouseSlug: guesthouse.slug,
          checkInDate,
          checkOutDate,
          adults,
          roomQuantities: roomQuantity > 0 ? { double: roomQuantity } : {},
        }}
        dispatch={vi.fn()}
        onBack={vi.fn()}
      />
    </AppProviders>,
  )
}

describe('BookingStayStep', () => {
  it('replaces continue with phone contact at the large-group boundary', async () => {
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)

    renderStep(20, 0)

    expect(await screen.findByText('Egyeztessünk telefonon')).toBeVisible()
    expect(screen.getByRole('link', { name: '+40 743 677 812' })).toHaveAttribute(
      'href',
      'tel:+40743677812',
    )
    expect(screen.queryByRole('button', { name: 'Tovább az adatokhoz' })).not.toBeInTheDocument()
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('requests a debounced quote only when room capacity exactly matches guests', async () => {
    const fetchMock = vi.fn<(input: RequestInfo | URL, init?: RequestInit) => Promise<Response>>(
      () =>
        Promise.resolve(
          okJson({
            currency: 'RON',
            nights: 3,
            totalGuests: 2,
            selectedRoomCount: 1,
            selectedCapacity: 2,
            lines: [],
            priceBreakdown: {
              accommodationTotal: 780,
              adultAccommodationTotal: 780,
              childAccommodationTotal: 0,
              singleRoomSurcharge: 0,
              breakfastTotal: 0,
              dinnerTotal: 0,
              totalPayable: 780,
            },
            requestOnly: true,
          }),
        ),
    )
    vi.stubGlobal('fetch', fetchMock)

    renderStep(2, 1)

    expect((await screen.findAllByText(/780/))[0]).toBeVisible()
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1))
    const quoteCall = fetchMock.mock.calls[0]
    expect(String(quoteCall[0])).toBe('/api/booking-quotes')
  })

  it('keeps entry fields usable and offers retry when the quote call fails', async () => {
    const fetchMock = vi.fn<(input: RequestInfo | URL, init?: RequestInit) => Promise<Response>>(
      () => Promise.resolve({ ok: false, status: 503, json: async () => ({}) } as Response),
    )
    vi.stubGlobal('fetch', fetchMock)

    renderStep(2, 1)

    expect(await screen.findByText(/Az ár most nem frissíthető/)).toBeVisible()
    expect(screen.getByLabelText('Érkezés')).toBeEnabled()
    expect(screen.getByLabelText('Távozás')).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Újrapróbálás' })).toBeEnabled()
  })
})
