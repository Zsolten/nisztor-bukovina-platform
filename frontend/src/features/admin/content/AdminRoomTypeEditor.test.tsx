import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { AdminAuthContext } from '../auth/adminAuthContext'
import AdminRoomTypeEditor from './AdminRoomTypeEditor'

const guesthouseId = '82b508e1-2893-4f45-8cc8-7a6f50b43a4d'
const roomType = {
  id: '104cd77c-1f4f-4e1c-924e-8f0654a70b01',
  code: 'double',
  quantity: 3,
  standardOccupancy: 2,
  roomsWithExtraBed: 0,
  extraBedsPerEligibleRoom: 0,
  active: true,
  displayOrder: 0,
  translations: [
    {
      language: 'hu' as const,
      name: 'Kétágyas szoba',
      shortDescription: 'Kényelmes szoba.',
      detailedDescription: '',
    },
    { language: 'ro' as const, name: '', shortDescription: '', detailedDescription: '' },
    { language: 'en' as const, name: '', shortDescription: '', detailedDescription: '' },
  ],
}

describe('AdminRoomTypeEditor', () => {
  it('saves the editable room-type quantity', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(response(200, [roomType]))
      .mockResolvedValueOnce(response(200, { ...roomType, quantity: 9 }))

    renderEditor(authorizedFetch)

    await user.click(await screen.findByRole('button', { name: /Szerkesztés/ }))
    const quantity = screen.getByLabelText('Darabszám')
    await user.clear(quantity)
    await user.type(quantity, '9')
    await user.click(screen.getByRole('button', { name: 'Szobatípus mentése' }))

    await waitFor(() => expect(authorizedFetch).toHaveBeenCalledTimes(2))
    expect(authorizedFetch).toHaveBeenLastCalledWith(
      `/api/admin/guesthouses/${guesthouseId}/room-types/${roomType.id}`,
      expect.objectContaining({ body: expect.stringContaining('"quantity":9'), method: 'PUT' }),
    )
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
      <AdminRoomTypeEditor
        guesthouseId={guesthouseId}
        guesthouseName="Nisztor Panzió"
        language="hu"
      />
    </AdminAuthContext>,
  )
}

function response(status: number, body: unknown) {
  return { json: async () => body, ok: status >= 200 && status < 300, status } as Response
}
