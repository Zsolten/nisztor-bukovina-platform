import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from './providers'
import { appRoutes } from './router'

const guesthouses = [
  {
    slug: 'nisztor-panzio',
    name: 'Nisztor Panzió',
    shortDescription: 'Csendes, nyugodt, családias környezet.',
    roomCount: 5,
    coverImage: {
      path: '/images/guesthouses/nisztor/gallery-01.jpg',
      altText: 'A Nisztor család a panzió előtt',
      cover: true,
    },
  },
  {
    slug: 'bukovina-panzio',
    name: 'Bukovina Panzió',
    shortDescription: 'Igényes szálláslehetőség Csernakeresztúron.',
    roomCount: 12,
    coverImage: {
      path: '/images/guesthouses/bukovina/gallery-01.jpg',
      altText: 'A Bukovina Panzió és a vendéglátó család',
      cover: true,
    },
  },
]

const nisztorDetail = {
  ...guesthouses[0],
  description: 'A Nisztor Panzió Csernakeresztúron található.',
  roomDescription: 'A panzió épületében 5 szoba található.',
  images: [guesthouses[0].coverImage],
  history: {
    title: 'Bukovinai székely örökség Csernakeresztúron',
    text: 'A helyi hagyományokat nemzedékről nemzedékre továbbadják.',
  },
  contacts: [
    { type: 'PHONE', value: '+40 743 677 812', label: 'Telefon', preferred: true },
    { type: 'EMAIL', value: 'nisztorpanzio@gmail.com', label: 'E-mail', preferred: false },
  ],
  address: {
    formatted: 'Str. Bucovina 17., Csernakeresztúr',
    latitude: 45.846,
    longitude: 22.897,
  },
  roomTypes: [
    {
      id: 'double',
      name: 'Kétágyas szoba',
      quantity: 3,
      standardOccupancy: 2,
      roomsWithExtraBed: 1,
      extraBedsPerEligibleRoom: 1,
      features: ['private-bathroom'],
    },
  ],
  amenities: [
    {
      id: 'wifi',
      name: 'Wi-Fi',
      description: 'Vezeték nélküli internetkapcsolat.',
      category: 'ROOM_COMFORT',
    },
  ],
  pricing: {
    currency: 'RON',
    items: [{ id: 'lodging', label: 'Szállás', amount: 130, unit: 'person_night' }],
    surcharges: [{ id: 'tourist-tax', label: 'Idegenforgalmi adó', percentage: 1 }],
    discounts: [{ id: 'children', label: 'Gyermekkedvezmény', percentage: 50 }],
    paymentNote: 'Fizetés érkezéskor.',
  },
}

function okJson(body: unknown): Response {
  return {
    ok: true,
    status: 200,
    json: async () => body,
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

describe('guesthouse and language routing', () => {
  beforeEach(() => {
    window.localStorage.clear()
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = String(input)
        return Promise.resolve(
          okJson(url.includes('/nisztor-panzio') ? nisztorDetail : guesthouses),
        )
      }),
    )
  })

  it('redirects the root path to Hungarian and lists both guesthouses separately', async () => {
    const router = renderRoute('/')

    await waitFor(() => expect(router.state.location.pathname).toBe('/hu'))
    expect(
      await screen.findByRole('heading', { name: 'Szeretettel köszöntjük honlapunkon!' }),
    ).toBeVisible()
    expect(await screen.findByRole('heading', { name: 'Nisztor Panzió' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Bukovina Panzió' })).toBeVisible()
    expect(screen.getAllByRole('link', { name: 'Megnézem a panziót' })).toHaveLength(2)
    expect(screen.getByRole('link', { name: /Nisztor Panzió/ })).toHaveAttribute(
      'href',
      '/hu/guesthouses/nisztor-panzio',
    )
    expect(screen.getByRole('link', { name: /Bukovina Panzió/ })).toHaveAttribute(
      'href',
      '/hu/guesthouses/bukovina-panzio',
    )
  })

  it('opens a guesthouse on its own detail route', async () => {
    const router = renderRoute('/hu/guesthouses/nisztor-panzio')

    expect(await screen.findByRole('heading', { name: 'Nisztor Panzió' })).toBeVisible()
    expect(screen.getByText('A panzió épületében 5 szoba található.')).toBeVisible()
    expect(screen.getByRole('link', { name: 'Vissza a panziókhoz' })).toHaveAttribute('href', '/hu')
    expect(router.state.location.pathname).toBe('/hu/guesthouses/nisztor-panzio')
  })

  it('redirects the root path to the remembered supported language', async () => {
    window.localStorage.setItem('preferredLanguage', 'ro')
    const router = renderRoute('/')

    await waitFor(() => expect(router.state.location.pathname).toBe('/ro'))
  })

  it('redirects an unsupported language to Hungarian', async () => {
    const router = renderRoute('/de')

    await waitFor(() => expect(router.state.location.pathname).toBe('/hu'))
  })

  it('stores a supported language when its route is opened', async () => {
    renderRoute('/en')

    await waitFor(() => expect(window.localStorage.getItem('preferredLanguage')).toBe('en'))
  })

  it('opens and closes the mobile language navigation', async () => {
    const user = userEvent.setup()
    renderRoute('/hu')

    const toggle = await screen.findByRole('button', { name: 'Menü megnyitása' })
    await user.click(toggle)

    expect(screen.getByRole('dialog', { name: 'Nyelvválasztás' })).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Menü bezárása' }))
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: 'Nyelvválasztás' })).not.toBeInTheDocument(),
    )
  })
})
