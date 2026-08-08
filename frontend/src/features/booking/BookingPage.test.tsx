import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import { appRoutes } from '../../app/router'
import type { GuesthouseDetail, GuesthouseSummary } from '../../shared/api/guesthouses'

const nisztorId = '10000000-0000-0000-0000-000000000001'
const bukovinaId = '10000000-0000-0000-0000-000000000002'

const guesthouses: GuesthouseSummary[] = [
  {
    id: nisztorId,
    slug: 'nisztor-panzio',
    name: 'Nisztor Panzió',
    shortDescription: 'Öt szobás, családias panzió.',
    roomCount: 5,
    coverImage: { path: '/nisztor.jpg', altText: 'Nisztor Panzió', cover: true },
  },
  {
    id: bukovinaId,
    slug: 'bukovina-panzio',
    name: 'Bukovina Panzió',
    shortDescription: 'Tágas panzió családok és csoportok számára.',
    roomCount: 12,
    coverImage: { path: '/bukovina.jpg', altText: 'Bukovina Panzió', cover: true },
  },
]

function detailFor(
  summary: GuesthouseSummary,
  roomName: string,
  occupancy: number,
  price: number,
  service: string,
): GuesthouseDetail {
  return {
    ...summary,
    description: summary.shortDescription,
    roomDescription: 'Szobák leírása.',
    images: [summary.coverImage],
    history: { title: 'Történet', text: 'A panzió története.' },
    contacts: [],
    address: { formatted: 'Csernakeresztúr', latitude: 45.84, longitude: 22.89 },
    roomTypes: [
      {
        id: `${summary.slug}-room`,
        name: roomName,
        quantity: 2,
        standardOccupancy: occupancy,
        roomsWithExtraBed: 0,
        extraBedsPerEligibleRoom: 0,
        features: [],
      },
    ],
    amenities: [
      {
        id: `${summary.slug}-service`,
        name: service,
        category: 'OUTDOOR_WELLNESS',
      },
    ],
    pricing: {
      currency: 'RON',
      items: [{ id: 'accommodation', label: 'Szállás', amount: price, unit: 'person_night' }],
      taxes: [],
      surcharges: [],
      discounts: [],
      paymentNote: 'Fizetés érkezéskor.',
    },
  }
}

const details: Record<string, GuesthouseDetail> = {
  'nisztor-panzio': detailFor(guesthouses[0], 'Háromágyas szoba', 3, 130, 'Nisztor wellness'),
  'bukovina-panzio': detailFor(guesthouses[1], 'Négyágyas szoba', 4, 140, 'Bukovina kert'),
}

details['bukovina-panzio'].pricing.items = [
  ...details['bukovina-panzio'].pricing.items,
  { id: 'breakfast', label: 'Reggeli', amount: 45, unit: 'person' },
  { id: 'dinner', label: 'Vacsora', amount: 75, unit: 'person' },
  { id: 'sauna', label: 'Szauna', amount: 80, unit: 'person' },
  { id: 'guide', label: 'Idegenvezetés', amount: 600, unit: 'day' },
]
details['bukovina-panzio'].amenities = [
  ...details['bukovina-panzio'].amenities,
  ...['Parkoló', 'Wi-Fi', 'Terasz', 'Közös konyha', 'Dézsafürdő', 'Bukovina szauna'].map(
    (name, index) => ({
      id: `bukovina-extra-${index}`,
      name,
      category: 'OUTDOOR_WELLNESS' as const,
    }),
  ),
]

function okJson(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as Response
}

function renderBooking(initialEntry: string) {
  const router = createMemoryRouter(appRoutes, { initialEntries: [initialEntry] })
  render(
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>,
  )
  return router
}

describe('BookingPage guesthouse entry step', () => {
  beforeEach(() => {
    window.localStorage.clear()
    Object.defineProperty(window, 'scrollY', { configurable: true, value: 0 })
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.includes('/api/guesthouses?')) return Promise.resolve(okJson(guesthouses))

        const slug = Object.keys(details).find((candidate) => url.includes(`/${candidate}?`))
        return Promise.resolve(okJson(slug ? details[slug] : guesthouses))
      }),
    )
  })

  it('keeps later flow content and submission disabled until a guesthouse is selected', async () => {
    renderBooking('/hu/booking')

    expect(await screen.findByRole('radio', { name: /Nisztor Panzió/ })).not.toBeChecked()
    expect(screen.getByRole('group', { name: 'A kiválasztott panzió részletei' })).toBeDisabled()
    expect(screen.getByRole('button', { name: 'Tovább a foglalási adatokhoz' })).toBeDisabled()
    expect(screen.getByText('A foglalási igényt a panzió visszajelzése véglegesíti.')).toBeVisible()
  })

  it('carries the selected id and refreshes guesthouse-specific content', async () => {
    const user = userEvent.setup()
    const router = renderBooking('/hu/booking')

    const bukovina = await screen.findByRole('radio', { name: /Bukovina Panzió/ })
    await user.click(bukovina)

    await waitFor(() =>
      expect(router.state.location.pathname).toBe('/hu/guesthouses/bukovina-panzio/booking'),
    )
    expect(bukovina).toHaveAttribute('value', bukovinaId)
    expect(bukovina).toBeChecked()
    expect(bukovina.closest('label')).toHaveClass('is-selected')
    expect(screen.queryByText('✓')).not.toBeInTheDocument()
    expect(await screen.findByText('Négyágyas szoba')).toBeVisible()
    expect(screen.getByText('140 RON')).toBeVisible()
    expect(screen.getByText('Bukovina kert')).toBeVisible()
    expect(screen.getByText('Idegenvezetés')).toBeVisible()
    expect(screen.queryByText('Bukovina szauna')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: '+1 további' }))
    expect(screen.getByText('Bukovina szauna')).toBeVisible()
    expect(screen.getByRole('button', { name: 'Kevesebb' })).toBeVisible()
    expect(screen.getByRole('button', { name: 'Tovább a foglalási adatokhoz' })).toBeEnabled()

    await user.click(screen.getByRole('radio', { name: /Nisztor Panzió/ }))

    await waitFor(() =>
      expect(router.state.location.pathname).toBe('/hu/guesthouses/nisztor-panzio/booking'),
    )
    expect(await screen.findByText('Háromágyas szoba')).toBeVisible()
    expect(screen.getByText('130 RON')).toBeVisible()
    expect(screen.getByText('Nisztor wellness')).toBeVisible()
    expect(screen.queryByText('Bukovina kert')).not.toBeInTheDocument()
  })

  it('preselects the guesthouse when entering from its detail route', async () => {
    renderBooking('/hu/guesthouses/nisztor-panzio/booking')

    const nisztor = await screen.findByRole('radio', { name: /Nisztor Panzió/ })
    await waitFor(() => expect(nisztor).toBeChecked())
    expect(nisztor).toHaveAttribute('value', nisztorId)
    expect(await screen.findByText('Nisztor wellness')).toBeVisible()
  })
})
