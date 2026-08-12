import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import { appRoutes } from '../../app/router'

function jsonResponse(status: number, body: unknown, headers: Record<string, string> = {}) {
  return {
    headers: new Headers(headers),
    json: async () => body,
    ok: status >= 200 && status < 300,
    status,
  } as Response
}

function renderRoute(initialEntry: string) {
  const router = createMemoryRouter(appRoutes, { initialEntries: [initialEntry] })
  render(
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>,
  )
  return router
}

async function signIn(user: ReturnType<typeof userEvent.setup>) {
  await user.type(screen.getByLabelText('E-mail-cím'), 'admin@example.com')
  await user.type(screen.getByLabelText('Jelszó'), 'correct-password')
  await user.click(screen.getByRole('button', { name: 'Belépés' }))
}

describe('administrator routing and authentication', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('redirects unauthenticated visitors from protected admin routes to login', async () => {
    const router = renderRoute('/admin/bookings')

    expect(await screen.findByRole('heading', { name: 'Üdvözöljük újra!' })).toBeVisible()
    expect(router.state.location.pathname).toBe('/admin/login')
  })

  it('opens the booking work queue after a successful login', async () => {
    const user = userEvent.setup()
    const fetchMock = vi.mocked(fetch)
    fetchMock.mockResolvedValue(
      jsonResponse(200, {
        accessToken: 'signed-admin-token',
        expiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
        tokenType: 'Bearer',
      }),
    )
    const router = renderRoute('/admin/login')

    await signIn(user)

    expect(await screen.findByRole('heading', { name: 'Foglalási kérelmek' })).toBeVisible()
    expect(router.state.location.pathname).toBe('/admin/bookings')
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/admin/auth/login',
      expect.objectContaining({ method: 'POST' }),
    )
  })

  it('shows generic feedback for invalid credentials', async () => {
    const user = userEvent.setup()
    vi.mocked(fetch).mockResolvedValue(jsonResponse(401, { code: 'INVALID_ADMIN_CREDENTIALS' }))
    renderRoute('/admin/login')

    await signIn(user)

    expect(await screen.findByRole('alert')).toHaveTextContent('Hibás e-mail-cím vagy jelszó.')
  })

  it('shows the wait time when the login rate limit is reached', async () => {
    const user = userEvent.setup()
    vi.mocked(fetch).mockResolvedValue(
      jsonResponse(429, { code: 'ADMIN_LOGIN_RATE_LIMITED' }, { 'Retry-After': '12' }),
    )
    renderRoute('/admin/login')

    await signIn(user)

    expect(await screen.findByRole('alert')).toHaveTextContent('Próbálja újra 12 másodperc múlva.')
  })

  it('removes access to protected routes after logout', async () => {
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
      .mockResolvedValueOnce(jsonResponse(204, null))
    const router = renderRoute('/admin/login')

    await signIn(user)
    expect(await screen.findByRole('heading', { name: 'Foglalási kérelmek' })).toBeVisible()
    await user.click(
      within(screen.getByRole('complementary', { name: 'Admin navigáció' })).getByRole('button', {
        name: 'Kijelentkezés',
      }),
    )

    expect(await screen.findByRole('heading', { name: 'Üdvözöljük újra!' })).toBeVisible()
    expect(router.state.location.pathname).toBe('/admin/login')
    expect(fetchMock).toHaveBeenLastCalledWith(
      '/api/admin/auth/logout',
      expect.objectContaining({
        headers: expect.objectContaining({ Authorization: 'Bearer signed-admin-token' }),
        method: 'POST',
      }),
    )
  })

  it('returns to login when the access token expires', async () => {
    const user = userEvent.setup()
    vi.mocked(fetch).mockResolvedValue(
      jsonResponse(200, {
        accessToken: 'short-lived-token',
        expiresAt: new Date(Date.now() + 30).toISOString(),
        tokenType: 'Bearer',
      }),
    )
    const router = renderRoute('/admin/login')

    await signIn(user)

    await waitFor(() => expect(router.state.location.pathname).toBe('/admin/login'))
    expect(screen.getByRole('alert')).toHaveTextContent('A munkamenet lejárt.')
  })
})
