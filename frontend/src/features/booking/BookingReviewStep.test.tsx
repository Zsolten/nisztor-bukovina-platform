import { render, screen, waitFor } from '@testing-library/react'
import { useReducer } from 'react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import type { Language } from '../../i18n/languages'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'
import BookingReviewStep from './BookingReviewStep'
import { bookingReducer, initialBookingFlowState } from './bookingReducer'

const guesthouse = {
  id: '10000000-0000-0000-0000-000000000001',
  slug: 'bukovina-panzio',
  name: 'Bukovina Panzió',
  shortDescription: 'Családias szállás.',
  roomCount: 19,
  coverImage: { path: '/cover.jpg', altText: 'Bukovina Panzió', cover: true },
  description: 'Leírás',
  roomDescription: 'Szobák',
  images: [],
  history: { title: 'Történet', text: 'Szöveg' },
  contacts: [],
  address: { formatted: 'Csernakeresztúr', latitude: 45, longitude: 22 },
  roomTypes: [
    {
      id: '20000000-0000-0000-0000-000000000001',
      name: 'Kétágyas szoba',
      quantity: 12,
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
    taxes: [],
    surcharges: [],
    discounts: [],
    paymentNote: 'Fizetés érkezéskor.',
  },
} satisfies GuesthouseDetail

const quote = {
  currency: 'RON' as const,
  nights: 2,
  totalGuests: 2,
  selectedRoomCount: 1,
  selectedCapacity: 2,
  lines: [],
  priceBreakdown: {
    accommodationTotal: 520,
    singleRoomSurcharge: 0,
    breakfastTotal: 0,
    dinnerTotal: 0,
    totalPayable: 520,
  },
  requestOnly: true as const,
}

afterEach(() => vi.unstubAllGlobals())

function requestHeader(init: RequestInit | undefined, name: string) {
  const headers = init?.headers
  if (!headers) return null
  if (headers instanceof Headers) return headers.get(name)
  if (Array.isArray(headers)) return headers.find(([key]) => key === name)?.[1] ?? null
  return headers[name] ?? null
}

function requestBody(init: RequestInit | undefined) {
  return typeof init?.body === 'string' ? init.body : ''
}

function ReviewHarness({
  onSubmitted,
  language = 'hu',
  preferredLanguage = 'hu',
}: {
  onSubmitted: () => void
  language?: Language
  preferredLanguage?: 'hu' | 'ro' | 'en' | ''
}) {
  const [state, dispatch] = useReducer(bookingReducer, {
    ...initialBookingFlowState,
    guesthouseId: guesthouse.id,
    guesthouseSlug: guesthouse.slug,
    checkInDate: '2026-09-10',
    checkOutDate: '2026-09-12',
    adults: 2,
    roomQuantities: { [guesthouse.roomTypes[0].id]: 1 },
    contact: {
      ...initialBookingFlowState.contact,
      preferredLanguage,
    },
  })

  return (
    <BookingReviewStep
      language={language}
      guesthouse={guesthouse}
      state={state}
      dispatch={dispatch}
      onBack={vi.fn()}
      onSubmitted={onSubmitted}
    />
  )
}

describe('BookingReviewStep', () => {
  it('defaults the contact language to the current interface language until the visitor chooses one', async () => {
    const user = userEvent.setup()
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve({ ok: true, json: async () => quote })),
    )

    render(
      <AppProviders>
        <ReviewHarness onSubmitted={vi.fn()} language="ro" preferredLanguage="hu" />
      </AppProviders>,
    )

    const languageSelect = screen.getByRole('combobox')
    await waitFor(() => expect(languageSelect).toHaveValue('ro'))

    await user.selectOptions(languageSelect, 'en')
    expect(languageSelect).toHaveValue('en')
  })

  it('submits one idempotent request when submit is triggered twice quickly', async () => {
    const user = userEvent.setup()
    let completeSubmission: (value: unknown) => void = () => undefined
    const submitResponse = new Promise((resolve) => {
      completeSubmission = resolve
    })
    const fetchMock = vi.fn<(path: string, init?: RequestInit) => Promise<unknown>>((path) => {
      if (path === '/api/booking-quotes') {
        return Promise.resolve({ ok: true, json: async () => quote })
      }
      return submitResponse.then(() => ({
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
      }))
    })
    vi.stubGlobal('fetch', fetchMock)
    const onSubmitted = vi.fn()

    render(
      <AppProviders>
        <ReviewHarness onSubmitted={onSubmitted} />
      </AppProviders>,
    )

    await screen.findAllByText('520 RON')
    expect(screen.queryByText('Egyágyas felár')).not.toBeInTheDocument()
    expect(screen.getByText('Érkezés')).toBeVisible()
    expect(screen.getByText('Távozás')).toBeVisible()
    await user.type(screen.getByRole('textbox', { name: 'Kapcsolattartó neve' }), 'Nisztor Zsolt')
    await user.type(screen.getByRole('textbox', { name: 'E-mail-cím' }), 'zsolt@example.com')
    await user.type(screen.getByRole('textbox', { name: 'Telefonszám' }), '+40743677812')

    const submitButton = screen.getByRole('button', { name: 'Foglalási igény elküldése' })
    await user.dblClick(submitButton)

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(2))
    expect(requestHeader(fetchMock.mock.calls[1]?.[1], 'Idempotency-Key')).toBeTruthy()

    completeSubmission(undefined)
    await waitFor(() => expect(onSubmitted).toHaveBeenCalledTimes(1))
  })

  it('requires explicit confirmation before resubmitting a changed price', async () => {
    const user = userEvent.setup()
    const updatedQuote = {
      ...quote,
      priceBreakdown: { ...quote.priceBreakdown, accommodationTotal: 650, totalPayable: 650 },
    }
    let submissionCount = 0
    const fetchMock = vi.fn<(path: string, init?: RequestInit) => Promise<unknown>>((path) => {
      if (path === '/api/booking-quotes') {
        return Promise.resolve({ ok: true, json: async () => quote })
      }
      submissionCount += 1
      if (submissionCount === 1) {
        return Promise.resolve({
          ok: false,
          status: 409,
          json: async () => ({
            code: 'BOOKING_PRICE_CHANGED',
            errors: [],
            currentQuote: updatedQuote,
          }),
        })
      }
      return Promise.resolve({
        ok: true,
        json: async () => ({
          reference: 'NB-0123456789ABCDEF',
          status: 'RECEIVED',
          currency: 'RON',
          nights: 2,
          totalGuests: 2,
          totalPayable: 650,
          requestOnly: true,
        }),
      })
    })
    vi.stubGlobal('fetch', fetchMock)
    const onSubmitted = vi.fn()

    render(
      <AppProviders>
        <ReviewHarness onSubmitted={onSubmitted} />
      </AppProviders>,
    )

    await screen.findAllByText('520 RON')
    await user.type(screen.getByRole('textbox', { name: 'Kapcsolattartó neve' }), 'Nisztor Zsolt')
    await user.type(screen.getByRole('textbox', { name: 'E-mail-cím' }), 'zsolt@example.com')
    await user.type(screen.getByRole('textbox', { name: 'Telefonszám' }), '+40743677812')
    const submitButton = screen.getByRole('button', { name: 'Foglalási igény elküldése' })

    await user.click(submitButton)
    const confirmation = await screen.findByRole('checkbox')
    expect(
      screen.getByText(
        'Az ár időközben megváltozott. Kérjük, ellenőrizze és fogadja el az új összeget.',
      ),
    ).toBeVisible()

    await user.click(submitButton)
    expect(fetchMock).toHaveBeenCalledTimes(2)

    await user.click(confirmation)
    await user.click(submitButton)

    await waitFor(() => expect(onSubmitted).toHaveBeenCalledTimes(1))
    expect(JSON.parse(requestBody(fetchMock.mock.calls[2]?.[1]))).toMatchObject({
      acceptedTotal: 650,
    })
  })
})
