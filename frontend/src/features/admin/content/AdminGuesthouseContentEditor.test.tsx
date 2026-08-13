import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AdminAuthContext } from '../auth/adminAuthContext'
import type {
  AdminGuesthouseContent,
  AdminGuesthouseTranslation,
} from '../api/adminGuesthouseContent'
import AdminGuesthouseContentEditor from './AdminGuesthouseContentEditor'

const hu: AdminGuesthouseTranslation = {
  language: 'hu',
  version: 0,
  name: 'Nisztor Panzió',
  shortDescription: 'Rövid leírás',
  description: 'Részletes leírás',
  roomDescription: 'Szobák bevezetője',
  historyTitle: 'Történet címe',
  historyText: 'Történet szövege',
}

const content: AdminGuesthouseContent[] = [
  {
    id: '82b508e1-2893-4f45-8cc8-7a6f50b43a4d',
    slug: 'nisztor-panzio',
    active: true,
    translations: [
      hu,
      { ...hu, language: 'ro', name: 'Pensiunea Nisztor' },
      { ...hu, language: 'en', name: 'Nisztor Guesthouse' },
    ],
  },
]

function response(status: number, body: unknown) {
  return {
    json: async () => body,
    ok: status >= 200 && status < 300,
    status,
  } as Response
}

function renderEditor(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
) {
  const router = createMemoryRouter(
    [
      { path: '/admin/content', element: <AdminGuesthouseContentEditor /> },
      { path: '/admin/bookings', element: <div>Foglalások</div> },
    ],
    { initialEntries: ['/admin/content'] },
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

afterEach(() => vi.restoreAllMocks())

describe('AdminGuesthouseContentEditor', () => {
  it('keeps language drafts and saves only the selected translation', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(response(200, content))
      .mockResolvedValueOnce(response(200, { ...hu, version: 1, name: 'Új Nisztor név' }))
    renderEditor(authorizedFetch)

    const name = await screen.findByLabelText('Panzió neve')
    await user.clear(name)
    await user.type(name, 'Új Nisztor név')
    await user.click(screen.getByRole('tab', { name: 'Román' }))
    expect(screen.getByLabelText('Panzió neve')).toHaveValue('Pensiunea Nisztor')
    await user.click(screen.getByRole('tab', { name: 'Magyar' }))
    expect(screen.getByLabelText('Panzió neve')).toHaveValue('Új Nisztor név')

    await user.click(screen.getByRole('button', { name: 'Fordítás mentése' }))

    await screen.findByText('A tartalom mentése sikerült.')
    expect(authorizedFetch).toHaveBeenLastCalledWith(
      '/api/admin/guesthouses/82b508e1-2893-4f45-8cc8-7a6f50b43a4d/translations/hu',
      expect.objectContaining({ method: 'PUT' }),
    )
    const request = authorizedFetch.mock.calls.at(-1)?.[1] as RequestInit
    expect(JSON.parse(request.body as string)).toMatchObject({
      language: 'hu',
      version: 0,
      name: 'Új Nisztor név',
    })
  })

  it('shows local field feedback without calling the update endpoint', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi.fn().mockResolvedValue(response(200, content))
    renderEditor(authorizedFetch)

    const name = await screen.findByLabelText('Panzió neve')
    await user.clear(name)
    await user.click(screen.getByRole('button', { name: 'Fordítás mentése' }))

    expect(await screen.findByText('A mező kitöltése kötelező.')).toBeVisible()
    expect(authorizedFetch).toHaveBeenCalledTimes(1)
  })

  it('requires an explicit action before overwriting a concurrent update', async () => {
    const user = userEvent.setup()
    const currentContent = { ...hu, version: 1, name: 'Másik admin változata' }
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(response(200, content))
      .mockResolvedValueOnce(
        response(409, {
          code: 'ADMIN_CONTENT_VERSION_CONFLICT',
          fieldErrors: {},
          currentContent,
        }),
      )
      .mockResolvedValueOnce(response(200, { ...hu, version: 2, name: 'Saját változat' }))
    renderEditor(authorizedFetch)

    const name = await screen.findByLabelText('Panzió neve')
    await user.clear(name)
    await user.type(name, 'Saját változat')
    await user.click(screen.getByRole('button', { name: 'Fordítás mentése' }))

    expect(await screen.findByText('Ezt a fordítást időközben más is módosította.')).toBeVisible()
    expect(authorizedFetch).toHaveBeenCalledTimes(2)
    await user.click(screen.getByRole('button', { name: 'Saját változat felülírása' }))

    await waitFor(() => expect(authorizedFetch).toHaveBeenCalledTimes(3))
    const request = authorizedFetch.mock.calls.at(-1)?.[1] as RequestInit
    expect(JSON.parse(request.body as string)).toMatchObject({
      version: 1,
      name: 'Saját változat',
    })
  })
})
