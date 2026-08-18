import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import { appRoutes } from '../../app/router'

const TOKEN = 'A'.repeat(43)
const summary = {
  reference: 'NB-1234567890ABCDEF',
  status: 'RECEIVED',
  guesthouse: {
    name: 'Bukovina Panzió',
    contacts: [
      { type: 'PHONE', value: '+40 743 677 812', label: 'Telefon', preferred: true },
      { type: 'EMAIL', value: 'guesthouse@example.com', label: 'E-mail', preferred: true },
    ],
  },
  stay: { checkInDate: '2026-09-01', checkOutDate: '2026-09-03', nights: 2 },
  guests: { adults: 2, childrenAge3to10: 1, childrenAge0to3: 0 },
  services: { breakfastParticipants: 3, dinnerParticipants: 0 },
  rooms: [{ name: 'Háromágyas szoba', quantity: 1 }],
  price: {
    accommodationTotal: 780,
    breakfastTotal: 270,
    dinnerTotal: 0,
    totalPayable: 1050,
    currency: 'RON',
  },
  cancellationAllowed: true,
} as const

function response(body: unknown, status = 200): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as Response
}

function renderPage() {
  const router = createMemoryRouter(appRoutes, {
    initialEntries: [`/hu/booking-management/${TOKEN}`],
  })
  render(
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>,
  )
}

describe('booking management page', () => {
  beforeEach(() => {
    window.localStorage.clear()
    Object.defineProperty(window, 'scrollY', { configurable: true, value: 0 })
  })

  it('shows the token-scoped summary and requires confirmation before cancellation', async () => {
    const fetchMock = vi.fn((_input: RequestInfo | URL, init?: RequestInit) => {
      if (init?.method === 'POST') return Promise.resolve(response(null, 204))
      return Promise.resolve(response(summary))
    })
    vi.stubGlobal('fetch', fetchMock)
    renderPage()

    expect(await screen.findByRole('heading', { name: 'Foglalási adatok' })).toBeVisible()
    expect(screen.getByText('NB-1234567890ABCDEF')).toBeVisible()
    expect(screen.getByText('1 × Háromágyas szoba')).toBeVisible()

    await userEvent.click(screen.getByRole('button', { name: 'Foglalási kérelem lemondása' }))
    expect(fetchMock).toHaveBeenCalledTimes(1)
    await userEvent.click(screen.getByRole('button', { name: 'Igen, lemondom' }))

    expect(
      await screen.findByRole('heading', { name: 'A foglalási kérelmet lemondta' }),
    ).toBeVisible()
    expect(fetchMock).toHaveBeenLastCalledWith(
      `/api/booking-management/${TOKEN}/cancellation`,
      expect.objectContaining({ method: 'POST' }),
    )
  })

  it('shows direct contacts instead of cancellation for a confirmed booking', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve(response({ ...summary, status: 'CONFIRMED', cancellationAllowed: false })),
      ),
    )
    renderPage()

    expect(await screen.findByText('Lemondás közvetlen egyeztetéssel')).toBeVisible()
    expect(
      screen
        .getAllByRole('link', { name: '+40 743 677 812' })
        .some((link) => link.getAttribute('href') === 'tel:+40 743 677 812'),
    ).toBe(true)
    expect(screen.queryByRole('button', { name: 'Foglalási kérelem lemondása' })).toBeNull()
  })

  it('does not expose backend details for an invalid management link', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve(response({ code: 'BOOKING_MANAGEMENT_LINK_INVALID' }, 404))),
    )
    renderPage()

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: 'A hivatkozás nem használható' })).toBeVisible(),
    )
    expect(screen.queryByText('BOOKING_MANAGEMENT_LINK_INVALID')).toBeNull()
  })
})
