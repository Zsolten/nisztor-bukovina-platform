import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AdminAuthContext } from '../auth/adminAuthContext'
import type { AdminBookingPage, AdminBookingSummary } from '../api/adminBookings'
import AdminBookingDetailPlaceholder from './AdminBookingDetailPlaceholder'
import AdminBookingQueue from './AdminBookingQueue'

function jsonResponse(status: number, body: unknown) {
  return {
    headers: new Headers(),
    json: async () => body,
    ok: status >= 200 && status < 300,
    status,
  } as Response
}

const booking: AdminBookingSummary = {
  id: '30000000-0000-0000-0000-000000000001',
  publicReference: 'NB-0000000000000025',
  guesthouseId: '10000000-0000-0000-0000-000000000001',
  guesthouseName: 'Bukovina Panzió',
  status: 'RECEIVED',
  checkInDate: '2026-09-10',
  checkOutDate: '2026-09-12',
  nights: 2,
  totalGuests: 3,
  contactName: 'Teszt Vendég',
  totalPayable: 780,
  currency: 'RON',
  createdAt: '2026-08-11T09:30:00Z',
}

function bookingPage(content: AdminBookingSummary[] = [booking]): AdminBookingPage {
  return {
    content,
    page: 0,
    size: 20,
    totalElements: content.length,
    totalPages: content.length ? 2 : 0,
  }
}

function renderQueue(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
) {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue(
      jsonResponse(200, [
        {
          id: booking.guesthouseId,
          name: booking.guesthouseName,
          slug: 'bukovina-panzio',
          shortDescription: '',
          roomCount: 19,
          coverImage: { path: '/cover.jpg', altText: '', cover: true },
        },
      ]),
    ),
  )
  const router = createMemoryRouter(
    [
      { path: '/admin/bookings', element: <AdminBookingQueue /> },
      { path: '/admin/bookings/:bookingId', element: <AdminBookingDetailPlaceholder /> },
    ],
    { initialEntries: ['/admin/bookings'] },
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

  return router
}

afterEach(() => vi.unstubAllGlobals())

describe('AdminBookingQueue', () => {
  it('distinguishes loading from loaded results and opens the selected booking', async () => {
    const user = userEvent.setup()
    let resolveRequest: (response: Response) => void = () => undefined
    const authorizedFetch = vi.fn(
      () =>
        new Promise<Response>((resolve) => {
          resolveRequest = resolve
        }),
    )
    const router = renderQueue(authorizedFetch)

    expect(screen.getByRole('status')).toHaveTextContent('Foglalások betöltése')
    resolveRequest(jsonResponse(200, bookingPage()))

    const row = await screen.findByRole('link', { name: new RegExp(booking.publicReference) })
    expect(row).toHaveTextContent('Bukovina Panzió')
    expect(row).toHaveTextContent('Teszt Vendég')
    expect(row).toHaveTextContent('3 fő')
    expect(row).toHaveTextContent('Beérkezett')
    await user.click(row)

    expect(router.state.location.pathname).toBe(`/admin/bookings/${booking.id}`)
    expect(screen.getByText(booking.id)).toBeVisible()
  })

  it('keeps the current rows visible while sorting refreshes the data', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(200, bookingPage()))
      .mockImplementation(() => new Promise<Response>(() => undefined))
    renderQueue(authorizedFetch)
    await screen.findByText(booking.publicReference)

    await user.click(screen.getByRole('button', { name: /Összeg/ }))

    expect(screen.getByText(booking.publicReference)).toBeVisible()
    expect(screen.queryByText('Foglalások betöltése')).not.toBeInTheDocument()
  })

  it('updates filters, sorting and numbered pagination', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi.fn().mockResolvedValue(jsonResponse(200, bookingPage()))
    renderQueue(authorizedFetch)
    await screen.findByText(booking.publicReference)

    expect(String(authorizedFetch.mock.calls[0][0])).toContain('sortBy=checkInDate')
    expect(String(authorizedFetch.mock.calls[0][0])).toContain('sortDirection=asc')
    expect(String(authorizedFetch.mock.calls[0][0])).toContain('status=RECEIVED')
    expect(screen.getByLabelText('Állapot')).toHaveValue('RECEIVED')

    await user.type(screen.getByLabelText('Keresés'), 'Teszt Vendég')
    await waitFor(() =>
      expect(authorizedFetch).toHaveBeenCalledWith(
        expect.stringContaining('search=Teszt+Vend%C3%A9g'),
        expect.anything(),
      ),
    )

    await user.selectOptions(screen.getByLabelText('Panzió'), booking.guesthouseId)
    await user.selectOptions(screen.getByLabelText('Állapot'), 'UNDER_REVIEW')

    await waitFor(() =>
      expect(authorizedFetch).toHaveBeenCalledWith(
        expect.stringContaining(`guesthouseId=${booking.guesthouseId}`),
        expect.anything(),
      ),
    )
    await waitFor(() =>
      expect(authorizedFetch).toHaveBeenCalledWith(
        expect.stringContaining('status=UNDER_REVIEW'),
        expect.anything(),
      ),
    )

    await user.click(screen.getByRole('button', { name: /Összeg/ }))
    await waitFor(() =>
      expect(authorizedFetch).toHaveBeenCalledWith(
        expect.stringContaining('sortBy=totalPayable'),
        expect.anything(),
      ),
    )

    await user.click(screen.getByRole('button', { name: '2' }))
    await waitFor(() =>
      expect(authorizedFetch).toHaveBeenCalledWith(
        expect.stringContaining('page=1'),
        expect.anything(),
      ),
    )
  })

  it('shows a dedicated empty state', async () => {
    renderQueue(vi.fn().mockResolvedValue(jsonResponse(200, bookingPage([]))))

    expect(await screen.findByText('Nincs találat')).toBeVisible()
    expect(screen.queryByRole('link', { name: /NB-/ })).not.toBeInTheDocument()
  })

  it('shows an API error and retries the request', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(500, { code: 'ERROR' }))
      .mockResolvedValueOnce(jsonResponse(200, bookingPage([])))
    renderQueue(authorizedFetch)

    expect(await screen.findByRole('alert')).toHaveTextContent('Nem sikerült betölteni')
    await user.click(screen.getByRole('button', { name: 'Újrapróbálás' }))

    expect(await screen.findByText('Nincs találat')).toBeVisible()
    expect(authorizedFetch).toHaveBeenCalledTimes(2)
  })
})
