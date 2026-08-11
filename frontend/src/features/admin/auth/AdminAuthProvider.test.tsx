import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AdminAuthProvider } from './AdminAuthProvider'
import { useAdminAuth } from './adminAuthContext'

function jsonResponse(status: number, body: unknown) {
  return {
    headers: new Headers(),
    json: async () => body,
    ok: status >= 200 && status < 300,
    status,
  } as Response
}

function AuthProbe() {
  const { authorizedFetch, isAuthenticated, login } = useAdminAuth()

  return (
    <>
      <output>{isAuthenticated ? 'authenticated' : 'anonymous'}</output>
      <button onClick={() => void login('admin@example.com', 'correct-password')}>Belépés</button>
      <button
        disabled={!isAuthenticated}
        onClick={() => void authorizedFetch('/api/admin/future-endpoint').catch(() => undefined)}
      >
        Védett kérés
      </button>
    </>
  )
}

describe('AdminAuthProvider', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('clears the in-memory session when a protected request is rejected', async () => {
    const user = userEvent.setup()
    const fetchMock = vi.mocked(fetch)
    fetchMock
      .mockResolvedValueOnce(
        jsonResponse(200, {
          accessToken: 'signed-admin-token',
          expiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
          tokenType: 'Bearer',
        }),
      )
      .mockResolvedValueOnce(jsonResponse(401, { code: 'invalid_token' }))
    render(
      <AdminAuthProvider>
        <AuthProbe />
      </AdminAuthProvider>,
    )

    await user.click(screen.getByRole('button', { name: 'Belépés' }))
    await waitFor(() => expect(screen.getByText('authenticated')).toBeVisible())
    await user.click(screen.getByRole('button', { name: 'Védett kérés' }))

    await waitFor(() => expect(screen.getByText('anonymous')).toBeVisible())
    const lastCall = fetchMock.mock.calls.at(-1)
    expect(lastCall?.[0]).toBe('/api/admin/future-endpoint')
    expect(new Headers(lastCall?.[1]?.headers).get('Authorization')).toBe(
      'Bearer signed-admin-token',
    )
  })
})
