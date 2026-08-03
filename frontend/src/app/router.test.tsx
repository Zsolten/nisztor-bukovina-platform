import { render, screen, waitFor } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it } from 'vitest'
import { AppProviders } from './providers'
import { appRoutes } from './router'

describe('language routing', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('redirects the root path to Hungarian by default', async () => {
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(router.state.location.pathname).toBe('/hu'))
    expect(await screen.findByRole('heading', { name: 'Nisztor-Bukovina Platform' })).toBeVisible()
  })

  it('redirects the root path to the remembered supported language', async () => {
    localStorage.setItem('preferredLanguage', 'ro')
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(router.state.location.pathname).toBe('/ro'))
  })

  it('redirects an unsupported language to Hungarian', async () => {
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/de'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(router.state.location.pathname).toBe('/hu'))
  })

  it('stores a supported language when its route is opened', async () => {
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/en'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await waitFor(() => expect(localStorage.getItem('preferredLanguage')).toBe('en'))
  })
})
