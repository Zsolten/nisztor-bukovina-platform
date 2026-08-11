import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import type { AdminBookingDetail as AdminBookingDetailData } from '../api/adminBookings'
import { AdminAuthContext } from '../auth/adminAuthContext'
import AdminBookingDetail from './AdminBookingDetail'

const bookingId = '30000000-0000-0000-0000-000000000025'

function detail(status: AdminBookingDetailData['status'] = 'RECEIVED'): AdminBookingDetailData {
  return {
    id: bookingId,
    publicReference: 'NB-0000000000000025',
    guesthouse: { id: '10000000-0000-0000-0000-000000000001', name: 'Bukovina Panzió' },
    stay: {
      checkInDate: '2026-09-10',
      checkOutDate: '2026-09-12',
      nights: 2,
      adults: 2,
      childrenAge3to10: 1,
      childrenAge0to3: 0,
    },
    contact: {
      name: 'Teszt Vendég',
      email: 'vendeg@example.com',
      phone: '+40 700 000 000',
      preferredLanguage: 'hu',
    },
    services: { breakfastParticipants: 3, dinnerParticipants: 2 },
    rooms: [
      {
        roomTypeId: '20000000-0000-0000-0000-000000000001',
        roomTypeName: 'Háromágyas szoba',
        quantity: 1,
      },
    ],
    priceSnapshot: {
      accommodationTotal: 600,
      singleRoomSurcharge: 0,
      breakfastTotal: 90,
      dinnerTotal: 180,
      totalPayable: 870,
      currency: 'RON',
    },
    status,
    statusHistory: [
      { status, changedAt: '2026-08-11T09:30:00Z', changedBy: 'ADMIN:owner@example.com' },
    ],
    guestNote: 'Csendes szobát kérünk.',
    internalNote: 'Visszahívást kér.',
    createdAt: '2026-08-11T09:30:00Z',
    updatedAt: '2026-08-11T09:30:00Z',
  }
}

function jsonResponse(status: number, body?: unknown) {
  return {
    headers: new Headers(),
    json: async () => body,
    ok: status >= 200 && status < 300,
    status,
  } as Response
}

function renderDetail(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
) {
  const router = createMemoryRouter(
    [{ path: '/admin/bookings/:bookingId', element: <AdminBookingDetail /> }],
    { initialEntries: [`/admin/bookings/${bookingId}`] },
  )
  render(
    <AdminAuthContext
      value={{
        accessToken: 'admin-token',
        authorizedFetch,
        clearRejectedSession: vi.fn(),
        expiresAt: '2030-01-01T00:00:00Z',
        isAuthenticated: true,
        login: vi.fn(),
        logout: vi.fn(),
        sessionEndReason: null,
      }}
    >
      <RouterProvider router={router} />
    </AdminAuthContext>,
  )
}

describe('AdminBookingDetail', () => {
  it('loads and displays the complete booking detail with only valid actions', async () => {
    renderDetail(vi.fn().mockResolvedValue(jsonResponse(200, detail())))

    expect(await screen.findByRole('heading', { name: 'NB-0000000000000025' })).toBeVisible()
    expect(screen.getByText('Teszt Vendég')).toBeVisible()
    expect(screen.getByText('Háromágyas szoba')).toBeVisible()
    expect(screen.getByText('Csendes szobát kérünk.')).toBeVisible()
    expect(screen.getByDisplayValue('Visszahívást kér.')).toBeVisible()
    expect(screen.getByRole('button', { name: 'Visszaigazolás' })).toBeVisible()
    expect(screen.getByRole('button', { name: 'Elutasítás' })).toBeVisible()
    expect(screen.queryByRole('button', { name: 'Ellenőrzés megkezdése' })).not.toBeInTheDocument()
  })

  it('handles the intermediate review status without an extra administrator action', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(200, detail()))
      .mockResolvedValueOnce(jsonResponse(204))
      .mockResolvedValueOnce(jsonResponse(204))
      .mockResolvedValueOnce(jsonResponse(200, detail('CONFIRMED')))
    renderDetail(authorizedFetch)

    await user.click(await screen.findByRole('button', { name: 'Visszaigazolás' }))
    await user.click(
      within(screen.getByRole('dialog')).getByRole('button', { name: 'Visszaigazolás' }),
    )

    await waitFor(() => expect(authorizedFetch).toHaveBeenCalledTimes(4))
    expect(authorizedFetch.mock.calls[1][0]).toBe(`/api/admin/bookings/${bookingId}/status`)
    expect(authorizedFetch.mock.calls[1][1]).toMatchObject({
      method: 'PATCH',
      body: JSON.stringify({ status: 'UNDER_REVIEW' }),
    })
    expect(authorizedFetch.mock.calls[2][1]).toMatchObject({
      method: 'PATCH',
      body: JSON.stringify({ status: 'CONFIRMED' }),
    })
    expect(await screen.findByText('Az állapot frissült: Visszaigazolt.')).toBeVisible()
  })

  it('requires explicit confirmation before confirm and reject actions', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(200, detail('UNDER_REVIEW')))
      .mockResolvedValueOnce(jsonResponse(204))
      .mockResolvedValueOnce(jsonResponse(200, detail('CONFIRMED')))
    renderDetail(authorizedFetch)

    await user.click(await screen.findByRole('button', { name: 'Visszaigazolás' }))
    expect(authorizedFetch).toHaveBeenCalledTimes(1)

    const dialog = screen.getByRole('dialog')
    await user.click(within(dialog).getByRole('button', { name: 'Visszaigazolás' }))

    await waitFor(() => expect(authorizedFetch).toHaveBeenCalledTimes(3))
    expect(await screen.findByText('Az állapot frissült: Visszaigazolt.')).toBeVisible()
  })

  it('shows localized workflow errors without exposing the backend code', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(200, detail('UNDER_REVIEW')))
      .mockResolvedValueOnce(jsonResponse(400, { code: 'INVALID_BOOKING_STATUS_TRANSITION' }))
    renderDetail(authorizedFetch)

    await user.click(await screen.findByRole('button', { name: 'Elutasítás' }))
    await user.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Elutasítás' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Ez az állapotváltás már nem hajtható végre.',
    )
    expect(screen.queryByText('INVALID_BOOKING_STATUS_TRANSITION')).not.toBeInTheDocument()
  })

  it('saves the internal note and keeps the refreshed value', async () => {
    const user = userEvent.setup()
    const refreshed = { ...detail(), internalNote: 'Egyeztetve telefonon.' }
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(200, detail()))
      .mockResolvedValueOnce(jsonResponse(204))
      .mockResolvedValueOnce(jsonResponse(200, refreshed))
    renderDetail(authorizedFetch)

    const note = await screen.findByRole('textbox', { name: 'Belső megjegyzés' })
    await user.clear(note)
    await user.type(note, 'Egyeztetve telefonon.')
    await user.click(screen.getByRole('button', { name: 'Mentés' }))

    await waitFor(() => expect(authorizedFetch).toHaveBeenCalledTimes(3))
    expect(screen.getByDisplayValue('Egyeztetve telefonon.')).toBeVisible()
    expect(await screen.findByText('A belső megjegyzés mentve.')).toBeVisible()
  })
})
