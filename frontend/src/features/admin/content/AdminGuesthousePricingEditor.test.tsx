import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { AdminAuthContext } from '../auth/adminAuthContext'
import AdminGuesthousePricingEditor from './AdminGuesthousePricingEditor'

const guesthouseId = '82b508e1-2893-4f45-8cc8-7a6f50b43a4d'
const pricing = {
  guesthouseId,
  currency: 'RON' as const,
  items: [
    { code: 'accommodation', label: 'Szállás', amount: 180, unit: 'person_night' as const },
    { code: 'single_room', label: 'Egyágyas szoba', amount: 200, unit: 'room_night' as const },
    { code: 'breakfast', label: 'Reggeli', amount: 45, unit: 'person' as const },
  ],
  surcharges: [],
  discounts: [{ code: 'children_under_10', label: 'Gyermekkedvezmény', percentage: 50 }],
}

describe('AdminGuesthousePricingEditor', () => {
  it('loads, edits, and saves the current guesthouse prices', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(response(200, pricing))
      .mockResolvedValueOnce(
        response(200, {
          ...pricing,
          items: [{ ...pricing.items[0], amount: 195 }, pricing.items[1]],
        }),
      )

    renderEditor(authorizedFetch)

    const accommodation = await screen.findByRole('spinbutton', { name: 'Szállás' })
    await user.clear(accommodation)
    await user.type(accommodation, '195')
    await user.click(screen.getByRole('button', { name: 'Árak mentése' }))

    await waitFor(() => expect(authorizedFetch).toHaveBeenCalledTimes(2))
    expect(authorizedFetch).toHaveBeenLastCalledWith(
      `/api/admin/guesthouses/${guesthouseId}/pricing`,
      expect.objectContaining({
        body: expect.stringContaining('"amount":195'),
        method: 'PUT',
      }),
    )
    expect(await screen.findByText(/Az árak mentése sikerült/)).toBeVisible()
  })
})

function renderEditor(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
) {
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
      <AdminGuesthousePricingEditor guesthouseId={guesthouseId} guesthouseName="Nisztor Panzió" />
    </AdminAuthContext>,
  )
}

function response(status: number, body: unknown) {
  return {
    json: async () => body,
    ok: status >= 200 && status < 300,
    status,
  } as Response
}
