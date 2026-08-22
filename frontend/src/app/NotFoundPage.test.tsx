import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { AppProviders } from './providers'
import NotFoundPage from './NotFoundPage'

describe('NotFoundPage', () => {
  it('shows a localized 404 page and preserves the language in the home link', async () => {
    const router = createMemoryRouter([{ path: '*', element: <NotFoundPage /> }], {
      initialEntries: ['/ro/pagina-inexistenta'],
    })

    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    expect(await screen.findByRole('heading', { name: 'Pagina nu a fost găsită.' })).toBeVisible()
    expect(screen.getByRole('link', { name: 'Înapoi la pagina principală' })).toHaveAttribute(
      'href',
      '/ro',
    )
  })
})
