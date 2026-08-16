import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import { appRoutes } from '../../app/router'

const tour = {
  slug: 'maros-mente-es-gyulafehervar',
  name: 'Maros mente és Gyulafehérvár',
  shortDescription: 'Egynapos történelmi körút.',
  detailedDescription: 'A túra teljes leírása.',
  mapColor: '#376C8A',
  tags: [],
  images: [
    {
      imageUrl: 'https://images.example.test/maros-mente.jpg',
      altText: 'Vár a Maros mentén',
    },
  ],
  stops: [
    {
      slug: 'deva-vara',
      name: 'Déva vára',
      latitude: 45.8763,
      longitude: 22.9008,
      googleMapsUrl: 'https://maps.google.com/?q=45.8763,22.9008',
      optional: false,
      visitDurationMinutes: 90,
    },
  ],
  totals: {
    travelDistanceMeters: 168000,
    travelDurationSeconds: 12000,
    visitDurationMinutes: 90,
    totalDurationSeconds: 17400,
    routeDataComplete: true,
  },
  routeStatus: 'READY' as const,
}

describe('StarTourDetailPage', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() => Promise.resolve({ ok: true, json: async () => tour } as Response)),
    )
  })

  it('loads the public detail, its gallery and every stop', async () => {
    const router = createMemoryRouter(appRoutes, {
      initialEntries: ['/hu/star-tours/maros-mente-es-gyulafehervar'],
    })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    expect(
      await screen.findByRole('heading', { level: 1, name: 'Maros mente és Gyulafehérvár' }),
    ).toBeVisible()
    expect(screen.getByRole('img', { name: 'Vár a Maros mentén' })).toHaveAttribute(
      'src',
      'https://images.example.test/maros-mente.jpg',
    )
    expect(screen.getByText('Déva vára')).toBeVisible()
  })
})
