import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from './providers'
import ApplicationErrorPage from './ApplicationErrorPage'

function BrokenPage(): never {
  throw new Error('Internal implementation detail')
}

describe('ApplicationErrorPage', () => {
  afterEach(() => vi.restoreAllMocks())

  it('replaces a route error with a localized visitor-facing page', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined)
    const router = createMemoryRouter(
      [
        {
          path: '/:lang',
          element: <BrokenPage />,
          errorElement: <ApplicationErrorPage />,
        },
      ],
      { initialEntries: ['/hu'] },
    )

    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    expect(
      await screen.findByRole('heading', { name: 'Egy pillanatra megakadtunk.' }),
    ).toBeVisible()
    expect(screen.getByRole('link', { name: 'Vissza a főoldalra' })).toHaveAttribute('href', '/hu')
    expect(screen.queryByText('Internal implementation detail')).not.toBeInTheDocument()
  })
})
