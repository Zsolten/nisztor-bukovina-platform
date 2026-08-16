import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import { appRoutes } from '../../app/router'

const attractions = [
  {
    slug: 'deva-vara',
    name: 'Déva vára',
    shortDescription: 'Középkori várrom a Maros völgye fölött.',
    detailedDescription: 'Déva várának részletes története.',
    admissionInformation: '9 lej/fő',
    practicalInformation: null,
    latitude: 45.8763,
    longitude: 22.9008,
    googleMapsUrl: 'https://maps.google.com/?q=45.8763,22.9008',
    recommendedVisitDurationMinutes: 90,
    collectionSlugs: ['maros-mente'],
  },
  {
    slug: 'paring-hegyseg',
    name: 'Páring-hegység',
    shortDescription: 'Hegyi panoráma és libegő.',
    detailedDescription: 'A Páring-hegység részletes bemutatása.',
    admissionInformation: null,
    practicalInformation: null,
    latitude: 45.3935,
    longitude: 23.4461,
    googleMapsUrl: 'https://maps.google.com/?q=45.3935,23.4461',
    recommendedVisitDurationMinutes: 120,
    collectionSlugs: ['hatszegi-medence'],
  },
]

const tours = [
  {
    slug: 'maros-mente-es-gyulafehervar',
    name: 'Maros mente és Gyulafehérvár',
    shortDescription: 'Egynapos történelmi körút.',
    detailedDescription: 'A túra részletes bemutatása.',
    mapColor: '#376C8A',
    tags: ['egynapos'],
    images: [],
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
    routeStatus: 'READY',
  },
]

const route = {
  tourSlug: 'maros-mente-es-gyulafehervar',
  routeStatus: 'READY',
  cached: true,
  base: { latitude: 45.8232811, longitude: 22.930933 },
  stops: [
    {
      waypointIndex: 1,
      slug: 'deva-vara',
      latitude: 45.8763,
      longitude: 22.9008,
      optional: false,
    },
  ],
  legs: [],
  totalDistanceMeters: 168000,
  totalDurationSeconds: 12000,
  failureReason: null,
  retryAfter: null,
}

function okJson(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as Response
}

describe('TourismMapPage', () => {
  beforeEach(() => {
    window.localStorage.clear()
    Object.defineProperty(window, 'scrollY', { configurable: true, value: 0 })
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.endsWith('/routes')) return Promise.resolve(okJson([route]))
        if (url.includes('/star-tours')) return Promise.resolve(okJson(tours))
        if (url.includes('/attractions')) return Promise.resolve(okJson(attractions))
        return Promise.resolve(okJson([]))
      }),
    )
  })

  it('loads tours, switches to the attraction list and filters by category', async () => {
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    expect(await screen.findByText('Maros mente és Gyulafehérvár')).toBeVisible()
    await waitFor(() =>
      expect(fetch).toHaveBeenCalledWith(
        '/api/tourism/star-tours/routes',
        expect.any(Object),
      ),
    )

    await user.click(screen.getByRole('tab', { name: 'Látnivalók' }))
    expect(screen.getByRole('heading', { level: 2, name: 'Látnivalók' })).toBeVisible()
    expect(screen.getByText('Déva vára')).toBeVisible()
    expect(screen.getByText('Páring-hegység')).toBeVisible()

    await user.click(screen.getByRole('button', { name: 'Vár' }))
    expect(screen.getByText('Déva vára')).toBeVisible()
    expect(screen.queryByText('Páring-hegység')).not.toBeInTheDocument()
  })

  it('stores favorites without persisting full tour content', async () => {
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await user.click(
      await screen.findByRole('button', {
        name: 'Maros mente és Gyulafehérvár kedvencnek jelölése',
      }),
    )

    expect(JSON.parse(window.localStorage.getItem('favoriteStarTours') ?? '[]')).toEqual([
      expect.objectContaining({ tourId: 'maros-mente-es-gyulafehervar' }),
    ])
  })
})
