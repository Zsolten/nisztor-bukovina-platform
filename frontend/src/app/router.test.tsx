import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from './providers'
import { appRoutes } from './router'

const guesthouses = [
  {
    id: '10000000-0000-0000-0000-000000000001',
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
    id: '10000000-0000-0000-0000-000000000002',
    slug: 'bukovina-panzio',
    name: 'Bukovina Panzió',
    shortDescription: 'Igényes szálláslehetőség Csernakeresztúron.',
    roomCount: 20,
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
  pageText: {
    storyEyebrow: 'Csendes pihenés Csernakeresztúron',
    storyTitle: 'Egy kis falu, ahol megáll az idő',
    diningEyebrow: 'Házias erdélyi ízek',
    diningTitle: 'Ételek, amelyek visszahívják vendégeinket',
    diningDescription: 'Hagyományos, házi készítésű ételek.',
    amenitiesTitle: 'Szolgáltatások',
    roomTypesTitle: 'Szobatípusok',
    pricingTitle: 'Árak és feltételek',
    historyEyebrow: 'Történetünk és örökségünk',
    galleryTitle: 'Képgaléria',
    galleryHint: 'A nagyításhoz válasszon egy képet.',
  },
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
    taxes: [{ id: 'city_tax', label: 'Idegenforgalmi adó', percentage: 1 }],
    surcharges: [],
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
    Object.defineProperty(window, 'scrollY', { configurable: true, value: 0 })
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
      await screen.findByRole('region', { name: 'Szeretettel várjuk Csernakeresztúron.' }),
    ).toBeVisible()
    expect(screen.getAllByAltText('Nisztor Panzió logója')).toHaveLength(2)
    expect(screen.getAllByAltText('Nisztor Panzió logója')[0]).toHaveAttribute(
      'src',
      '/images/logo/nisztor-logo.png',
    )
    expect(screen.getAllByAltText('Bukovina Panzió logója')).toHaveLength(2)
    expect(screen.getAllByAltText('Bukovina Panzió logója')[0]).toHaveAttribute(
      'src',
      '/images/logo/bukovina-logo.png',
    )
    expect(document.querySelectorAll('.hero-slide')).toHaveLength(4)
    expect(screen.getByRole('heading', { name: 'Ismerje meg panzióinkat.' })).toBeVisible()
    expect(await screen.findByRole('heading', { name: 'Nisztor Panzió' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Bukovina Panzió' })).toBeVisible()
    expect(screen.getAllByRole('link', { name: 'Megnézem a panziót' })).toHaveLength(2)
    const bookingLinks = screen.getAllByRole('link', { name: 'Foglalás' })
    expect(bookingLinks).toHaveLength(3)
    bookingLinks.forEach((link) => expect(link).toHaveAttribute('href', '/hu/booking'))

    const nisztorCard = screen.getByRole('heading', { name: 'Nisztor Panzió' }).closest('article')
    expect(nisztorCard).not.toBeNull()

    const nisztorCardCopy = nisztorCard?.querySelector('.guesthouse-card-copy')
    expect(nisztorCardCopy).not.toBeNull()
    expect([...nisztorCardCopy!.children].map((element) => element.tagName)).toEqual([
      'P',
      'H3',
      'DIV',
      'A',
    ])
    expect(within(nisztorCard!).getByRole('link', { name: 'Megnézem a panziót' })).toBeVisible()
    expect(within(nisztorCard!).getByRole('link', { name: 'Foglalás' })).toBeVisible()

    expect(
      screen
        .getAllByRole('link', { name: /Nisztor Panzió/ })
        .some((link) => link.getAttribute('href') === '/hu/guesthouses/nisztor-panzio'),
    ).toBe(true)
    expect(
      screen
        .getAllByRole('link', { name: /Bukovina Panzió/ })
        .some((link) => link.getAttribute('href') === '/hu/guesthouses/bukovina-panzio'),
    ).toBe(true)
    expect(screen.getByRole('heading', { name: 'Családi vendéglátás közel 30 éve.' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Javasolt kirándulási irányok' })).toBeVisible()
    expect(screen.getByText('Déva, Vajdahunyad, Gyulafehérvár')).toBeVisible()
    expect(screen.getByRole('link', { name: 'Látnivalók felfedezése' })).toHaveAttribute(
      'href',
      '/hu/star-tours?view=attractions',
    )
    expect(
      screen.getByRole('link', {
        name: 'Déva, Vajdahunyad, Gyulafehérvár – Látnivalók felfedezése',
      }),
    ).toHaveAttribute('href', '/hu/star-tours?view=attractions')
    expect(screen.getByRole('heading', { name: 'Vendégeink mondták' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Így talál meg minket.' })).toBeVisible()
    expect(screen.getByTitle('A panziók helye a Google Térképen')).toHaveAttribute(
      'src',
      'https://www.google.com/maps?q=45.8234696,22.9360273&z=17&output=embed',
    )
    expect(screen.getByRole('link', { name: 'Útvonaltervezés Google Térképen' })).toHaveAttribute(
      'href',
      'https://www.google.com/maps/place/Pensiunea+Bukovina/@45.8232847,22.9322946,17z/data=!4m9!3m8!1s0x474e8c95c0541131:0xc8782fdf2f1f66e1!5m2!4m1!1i2!8m2!3d45.823281!4d22.9348695!16s%2Fg%2F11ddwzk7fn?entry=ttu&g_ep=EgoyMDI2MDgxMS4wIKXMDSoASAFQAw%3D%3D',
    )
    expect(screen.getByRole('link', { name: 'Foglalási kérelem' })).toHaveAttribute(
      'href',
      '/hu/booking',
    )
    expect(
      document.querySelector('.section-index, .card-number, .destination-image-wrap > span'),
    ).not.toBeInTheDocument()
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

  it('keeps an unknown localized URL and shows the not-found page', async () => {
    const router = renderRoute('/en/does-not-exist')

    expect(await screen.findByRole('heading', { name: 'Page not found.' })).toBeVisible()
    expect(router.state.location.pathname).toBe('/en/does-not-exist')
    expect(screen.getByRole('link', { name: 'Back to home' })).toHaveAttribute('href', '/en')
  })

  it('scrolls to the top after navigating to a different public page', async () => {
    const scrollTo = vi.fn()
    vi.stubGlobal('scrollTo', scrollTo)
    const router = renderRoute('/hu')

    await screen.findByRole('heading', { name: 'Ismerje meg panzióinkat.' })
    scrollTo.mockClear()

    await router.navigate('/hu/guesthouses/nisztor-panzio')

    await waitFor(() =>
      expect(scrollTo).toHaveBeenCalledWith({ top: 0, left: 0, behavior: 'auto' }),
    )
  })

  it('stores a supported language when its route is opened', async () => {
    renderRoute('/en')

    await waitFor(() => expect(window.localStorage.getItem('preferredLanguage')).toBe('en'))
  })

  it('changes the header treatment after scrolling', async () => {
    renderRoute('/hu')

    const header = document.querySelector('.site-header')
    expect(header).not.toHaveClass('site-header-scrolled')

    Object.defineProperty(window, 'scrollY', { configurable: true, value: 25 })
    window.dispatchEvent(new Event('scroll'))
    await waitFor(() => expect(header).toHaveClass('site-header-scrolled'))

    Object.defineProperty(window, 'scrollY', { configurable: true, value: 0 })
    window.dispatchEvent(new Event('scroll'))
    await waitFor(() => expect(header).not.toHaveClass('site-header-scrolled'))
  })

  it('keeps the light header treatment on inner pages', async () => {
    renderRoute('/hu/guesthouses/nisztor-panzio')

    expect(document.querySelector('.site-header')).toHaveClass('site-header-scrolled')
  })

  it.each([
    [
      '/ro',
      'Ospitalitate de familie de aproape 30 de ani.',
      'Exemplu de recenzie',
      'Patru membri ai familiei Nisztor în pensiune',
    ],
    [
      '/en',
      'Nearly 30 years of family hospitality.',
      'Sample guest review',
      'Four members of the Nisztor family inside the guesthouse',
    ],
  ])(
    'renders translated editorial content for %s',
    async (route, legacyTitle, reviewLabel, imageAlt) => {
      renderRoute(route)

      expect(await screen.findByRole('heading', { name: legacyTitle })).toBeVisible()
      expect(screen.getByText(reviewLabel)).toBeVisible()
      expect(screen.getByAltText(imageAlt)).toBeVisible()
    },
  )

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

  it('opens guesthouse navigation links and routes to the booking start', async () => {
    const user = userEvent.setup()
    const router = renderRoute('/hu/guesthouses/nisztor-panzio')

    const navigation = document.querySelector('.site-navigation')
    expect(navigation).not.toBeNull()
    const guesthouseHomeLink = within(navigation as HTMLElement).getByRole('link', {
      name: 'Panzióink',
    })
    expect(guesthouseHomeLink).toHaveAttribute('href', '/hu')
    await user.click(guesthouseHomeLink)
    await waitFor(() => expect(router.state.location.pathname).toBe('/hu'))

    await user.click(screen.getByRole('button', { name: 'Panziók listájának megnyitása' }))

    const guesthouseMenu = document.querySelector('.guesthouse-navigation-menu')
    expect(guesthouseMenu).toBeInTheDocument()
    expect(guesthouseMenu?.parentElement).toHaveClass('is-open')
    expect(
      within(guesthouseMenu as HTMLElement).getByRole('link', { name: 'Nisztor Panzió' }),
    ).toHaveAttribute('href', '/hu/guesthouses/nisztor-panzio')

    await user.click(within(guesthouseMenu as HTMLElement).getByRole('link', { name: 'Foglalás' }))
    await waitFor(() => expect(router.state.location.pathname).toBe('/hu/booking'))
  })
})
