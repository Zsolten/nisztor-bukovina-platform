import { fireEvent, render, screen, waitFor } from '@testing-library/react'
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
    detailedDescription:
      'Déva várának részletes története.\n\nA vár történetének második bekezdése.',
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

const localHeritageHouse = {
  slug: 'csernakereszturi-tajhaz',
  name: 'Csernakeresztúri tájház',
  shortDescription: 'Bukovinai székely tárgyakat bemutató helyi tájház.',
  detailedDescription: 'A tájház a helyi közösség emlékeit őrzi.',
  admissionInformation: 'Ingyenes.',
  practicalInformation: null,
  latitude: 45.8233,
  longitude: 22.9309,
  googleMapsUrl: 'https://maps.google.com/?q=45.8233,22.9309',
  recommendedVisitDurationMinutes: 30,
  collectionSlugs: ['maros-mente'],
}

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
  {
    slug: 'paring-es-hatszegi-medence',
    name: 'Páring-hegység és Hátszegi-medence',
    shortDescription: 'Egynapos hegyi kirándulás.',
    detailedDescription: 'A Páring-hegység részletes bemutatása.',
    mapColor: '#A84930',
    tags: ['hegyek'],
    images: [],
    stops: [
      {
        slug: 'paring-hegyseg',
        name: 'Páring-hegység',
        latitude: 45.3935,
        longitude: 23.4461,
        googleMapsUrl: 'https://maps.google.com/?q=45.3935,23.4461',
        optional: false,
        visitDurationMinutes: 120,
      },
    ],
    totals: {
      travelDistanceMeters: 146000,
      travelDurationSeconds: 10800,
      visitDurationMinutes: 120,
      totalDurationSeconds: 18000,
      routeDataComplete: false,
    },
    routeStatus: 'MISSING',
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
      'matchMedia',
      vi.fn(() => ({
        matches: false,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    )
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
    expect(document.querySelectorAll('.tourism-tour-card > img')).toHaveLength(tours.length)
    await waitFor(() =>
      expect(fetch).toHaveBeenCalledWith('/api/tourism/star-tours/routes', expect.any(Object)),
    )

    await user.click(screen.getByRole('tab', { name: 'Látnivalók' }))
    expect(screen.getByRole('heading', { level: 2, name: 'Látnivalók' })).toBeVisible()
    expect(screen.getByText('Déva vára')).toBeVisible()
    expect(screen.getByText('Páring-hegység')).toBeVisible()
    expect(document.querySelectorAll('.tourism-attraction-card > img')).toHaveLength(
      attractions.length,
    )

    await user.click(screen.getByRole('button', { name: 'Vár' }))
    expect(screen.getByText('Déva vára')).toBeVisible()
    expect(screen.queryByText('Páring-hegység')).not.toBeInTheDocument()

    const attractionCard = screen.getByText('Déva vára').closest('article')
    expect(attractionCard).not.toBeNull()
    await user.click(attractionCard!)
    const dialog = await screen.findByRole('dialog')
    const summary = dialog.querySelector('.tourism-modal-quote')
    const admissionHeading = screen.getByRole('heading', { name: 'Belépési információk' })
    const detailedDescriptionHeading = screen.getByRole('heading', { name: 'Részletes leírás' })

    expect(summary).toHaveTextContent('Középkori várrom a Maros völgye fölött.')
    expect(summary?.tagName).toBe('BLOCKQUOTE')
    expect(dialog.querySelector('.modal-header .tourism-attraction-category')).toHaveTextContent(
      'Vár',
    )
    expect(dialog.querySelector('.modal-header .tourism-modal-map-link')).toHaveAttribute(
      'href',
      'https://maps.google.com/?q=45.8763,22.9008',
    )
    expect(summary?.compareDocumentPosition(admissionHeading)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    )
    expect(admissionHeading.compareDocumentPosition(detailedDescriptionHeading)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    )
    expect(dialog.querySelectorAll('.tourism-modal-detailed-description p')).toHaveLength(2)

    await user.keyboard('{Escape}')
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })

  it('uses the Csernakeresztúri tájház as the initial attraction when available', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        if (url.endsWith('/routes')) return Promise.resolve(okJson([route]))
        if (url.includes('/star-tours')) return Promise.resolve(okJson(tours))
        if (url.includes('/attractions')) {
          return Promise.resolve(okJson([...attractions, localHeritageHouse]))
        }
        return Promise.resolve(okJson([]))
      }),
    )
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await user.click(await screen.findByRole('tab', { name: 'Látnivalók' }))
    expect(document.querySelector('.tourism-attraction-card.selected')).toHaveTextContent(
      'Csernakeresztúri tájház',
    )
  })

  it('shows category controls only in the mobile attraction list view', async () => {
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({
        matches: true,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    )
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await user.click(await screen.findByRole('tab', { name: 'Látnivalók' }))
    expect(screen.queryByRole('button', { name: 'Vár' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Lista nézet' }))

    expect(screen.getByText('Déva vára')).toBeVisible()
    expect(screen.getByText('Páring-hegység')).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Vár' }))
    expect(screen.queryByText('Páring-hegység')).not.toBeInTheDocument()
  })

  it('scrolls desktop category filters with the forward control', async () => {
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await user.click(await screen.findByRole('tab', { name: 'Látnivalók' }))
    const filters = screen.getByLabelText('Látnivaló-kategóriák')
    const scrollBy = vi.fn()
    Object.defineProperties(filters, {
      clientWidth: { configurable: true, value: 160 },
      scrollLeft: { configurable: true, value: 0, writable: true },
      scrollWidth: { configurable: true, value: 480 },
    })
    Object.defineProperty(filters, 'scrollBy', { configurable: true, value: scrollBy })

    fireEvent.scroll(filters)

    const forwardButton = await screen.findByRole('button', { name: 'További kategóriák' })
    expect(forwardButton).toBeEnabled()
    await user.click(forwardButton)
    expect(scrollBy).toHaveBeenCalledWith({ behavior: 'smooth', left: 180 })
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

  it('keeps focus in the search field while filtering accented tour names', async () => {
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    const searchInput = await screen.findByPlaceholderText('Hová indulnál?')
    await user.type(searchInput, 'gyulafehervar')

    expect(searchInput).toHaveFocus()
    expect(screen.getByRole('button', { name: 'Megnézem' })).toBeVisible()
  })

  it('shows a compact search result list without replacing the map on mobile', async () => {
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({
        matches: true,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    )
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    const searchInput = await screen.findByPlaceholderText('Hová indulnál?')
    await user.type(searchInput, 'paring')

    expect(searchInput).toHaveFocus()
    expect(screen.getByRole('region', { name: 'Keresési találatok' })).toHaveTextContent(
      'Páring-hegység és Hátszegi-medence',
    )
    expect(screen.queryByRole('button', { name: 'Megnézem' })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Páring-hegység és Hátszegi-medence' }))

    expect(screen.queryByRole('region', { name: 'Keresési találatok' })).not.toBeInTheDocument()
    expect(searchInput).toHaveValue('')
    expect(searchInput).not.toHaveFocus()
    expect(screen.getAllByRole('button', { name: 'Megnézem' }).length).toBeGreaterThan(0)
  })

  it('selects the first mobile search result and closes the keyboard on Enter', async () => {
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({
        matches: true,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    )
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    const searchInput = await screen.findByPlaceholderText('Hová indulnál?')
    await user.type(searchInput, 'paring{Enter}')

    expect(searchInput).toHaveValue('')
    expect(searchInput).not.toHaveFocus()
    expect(screen.queryByRole('region', { name: 'Keresési találatok' })).not.toBeInTheDocument()
    expect(
      screen.getByRole('button', { name: 'Páring-hegység és Hátszegi-medence megjelenítése' }),
    ).toHaveAttribute('aria-current', 'true')
  })

  it('switches star tours and attractions with horizontal swipes', async () => {
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    const tourCard = (await screen.findByText('Maros mente és Gyulafehérvár')).closest('article')
    expect(tourCard).not.toBeNull()
    fireEvent.touchStart(tourCard!, { touches: [{ clientX: 280, clientY: 150 }] })
    fireEvent.touchMove(tourCard!, { touches: [{ clientX: 180, clientY: 150 }] })
    fireEvent.touchEnd(tourCard!, { changedTouches: [{ clientX: 180, clientY: 150 }] })
    expect(
      screen.getByRole('button', { name: 'Páring-hegység és Hátszegi-medence megjelenítése' }),
    ).toHaveAttribute('aria-current', 'true')
    expect(screen.getByText('Páring-hegység és Hátszegi-medence').closest('article')).toHaveClass(
      'tourism-card-enter-next',
    )

    await user.click(screen.getByRole('tab', { name: 'Látnivalók' }))
    const attractionCard = screen.getByText('Déva vára').closest('article')
    expect(attractionCard).not.toBeNull()
    fireEvent.touchStart(attractionCard!, { touches: [{ clientX: 280, clientY: 150 }] })
    fireEvent.touchMove(attractionCard!, { touches: [{ clientX: 180, clientY: 150 }] })
    fireEvent.touchEnd(attractionCard!, { changedTouches: [{ clientX: 180, clientY: 150 }] })
    expect(screen.getByRole('button', { name: 'Páring-hegység megjelenítése' })).toHaveAttribute(
      'aria-current',
      'true',
    )
    expect(screen.getByText('Páring-hegység').closest('article')).toHaveClass(
      'tourism-card-enter-next',
    )
  })

  it('opens tour details when the mobile card is swiped upward', async () => {
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    const heading = await screen.findByRole('heading', {
      name: 'Maros mente és Gyulafehérvár',
    })
    const card = heading.closest('article')
    expect(card).not.toBeNull()

    fireEvent.touchStart(card!, { touches: [{ clientX: 180, clientY: 320 }] })
    fireEvent.touchMove(card!, { touches: [{ clientX: 180, clientY: 240 }] })
    fireEvent.touchEnd(card!, { changedTouches: [{ clientX: 180, clientY: 240 }] })

    const dialog = await screen.findByRole('dialog')
    const summary = dialog.querySelector('.tourism-modal-quote')
    const stopsHeading = screen.getByRole('heading', { name: 'Megállók' })
    const detailedDescriptionHeading = screen.getByRole('heading', { name: 'Részletes leírás' })

    expect(summary).toHaveTextContent('Egynapos történelmi körút.')
    expect(summary?.tagName).toBe('BLOCKQUOTE')
    expect(summary?.compareDocumentPosition(stopsHeading)).toBe(Node.DOCUMENT_POSITION_FOLLOWING)
    expect(stopsHeading.compareDocumentPosition(detailedDescriptionHeading)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING,
    )
    expect(screen.getByText('Utazási idő').parentElement).toHaveTextContent('3 óra 20 perc')
    expect(screen.getByText('Teljes látogatási idő').parentElement).toHaveTextContent(
      '4 óra 50 perc',
    )
    expect(dialog).toHaveTextContent('A túra részletes bemutatása.')
    const directionsLink = screen.getByRole('link', {
      name: 'Teljes túra megnyitása Google Mapsben',
    })
    expect(directionsLink.closest('.modal-header')).not.toBeNull()
    expect(directionsLink).toHaveTextContent('Google Térkép')
    expect(directionsLink).toHaveAttribute('href', expect.stringContaining('api=1'))
    expect(directionsLink).toHaveAttribute('target', '_blank')
  })

  it('opens the mobile bottom cards from their main surface and keeps Maps separate', async () => {
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({
        matches: true,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    )
    const user = userEvent.setup()
    const router = createMemoryRouter(appRoutes, { initialEntries: ['/hu/star-tours'] })
    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    await user.click(
      await screen.findByRole('button', {
        name: 'Maros mente és Gyulafehérvár kiválasztása',
      }),
    )
    expect(await screen.findByRole('dialog')).toHaveTextContent('Egynapos történelmi körút.')

    await user.click(screen.getByRole('button', { name: 'Részletek bezárása' }))
    await user.click(screen.getByRole('tab', { name: 'Látnivalók' }))

    const attractionCard = screen.getByText('Déva vára').closest('article')
    expect(attractionCard).not.toBeNull()
    expect(attractionCard?.querySelector('a.tourism-modal-map-link')).toHaveTextContent(
      'Google Térkép',
    )

    await user.click(attractionCard!)
    expect(await screen.findByRole('dialog')).toHaveTextContent('Déva várának részletes története.')
  })
})
