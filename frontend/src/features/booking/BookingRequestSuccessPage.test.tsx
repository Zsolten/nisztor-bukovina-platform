import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { MemoryRouter, Outlet, Route, Routes } from 'react-router-dom'
import { AppProviders } from '../../app/providers'
import i18n from '../../i18n/config'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'
import { initialBookingFlowState } from './bookingReducer'
import BookingRequestSuccessPage from './BookingRequestSuccessPage'

const guesthouse = {
  id: '10000000-0000-0000-0000-000000000001',
  slug: 'bukovina-panzio',
  name: 'Bukovina Panzió',
  shortDescription: 'Családias szállás.',
  roomCount: 20,
  coverImage: { path: '/cover.jpg', altText: 'Bukovina Panzió', cover: true },
  description: '',
  roomDescription: '',
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
  history: { title: '', text: '' },
  contacts: [
    { type: 'PHONE', value: '+40743677812', label: 'Telefon', preferred: true },
    { type: 'EMAIL', value: 'hello@example.com', label: 'E-mail', preferred: true },
  ],
  address: { formatted: 'Csernakeresztúr', latitude: 45, longitude: 22 },
  roomTypes: [
    {
      id: '20000000-0000-0000-0000-000000000001',
      name: 'Kétágyas szoba',
      quantity: 12,
      standardOccupancy: 2,
      features: [],
    },
  ],
  amenities: [],
  pricing: {
    currency: 'RON',
    items: [],
    taxes: [],
    surcharges: [],
    discounts: [],
    paymentNote: '',
  },
} satisfies GuesthouseDetail

function SuccessRoute() {
  return <Outlet context={{ language: 'hu' as const }} />
}

describe('BookingRequestSuccessPage', () => {
  beforeEach(async () => {
    await i18n.changeLanguage('hu')
  })

  it('shows the submission response, concise summary, and guesthouse contacts', () => {
    render(
      <AppProviders>
        <MemoryRouter
          initialEntries={[
            {
              pathname: '/hu/booking-request-success',
              state: {
                booking: {
                  reference: 'NB-0123456789ABCDEF',
                  status: 'RECEIVED',
                  currency: 'RON',
                  nights: 2,
                  totalGuests: 2,
                  totalPayable: 520,
                  requestOnly: true,
                },
                guesthouse,
                bookingState: {
                  ...initialBookingFlowState,
                  checkInDate: '2026-09-10',
                  checkOutDate: '2026-09-12',
                  roomQuantities: { [guesthouse.roomTypes[0].id]: 1 },
                },
              },
            },
          ]}
        >
          <Routes>
            <Route path="/:lang" element={<SuccessRoute />}>
              <Route path="booking-request-success" element={<BookingRequestSuccessPage />} />
            </Route>
          </Routes>
        </MemoryRouter>
      </AppProviders>,
    )

    expect(screen.getByText('NB-0123456789ABCDEF')).toBeVisible()
    expect(screen.getByText('Beérkezett')).toBeVisible()
    expect(screen.getByText(/520/)).toBeVisible()
    expect(screen.getByRole('link', { name: '+40743677812' })).toHaveAttribute(
      'href',
      'tel:+40743677812',
    )
    expect(screen.getByRole('link', { name: 'hello@example.com' })).toHaveAttribute(
      'href',
      'mailto:hello@example.com',
    )
  })
})
