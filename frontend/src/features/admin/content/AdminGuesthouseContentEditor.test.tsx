import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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
  storyEyebrow: 'Bemutatkozás label',
  storyTitle: 'Bemutatkozás címe',
  diningEyebrow: 'Étkezés label',
  diningTitle: 'Étkezés címe',
  diningDescription: 'Étkezés leírása',
  amenitiesTitle: 'Szolgáltatások',
  roomTypesTitle: 'Szobatípusok',
  pricingTitle: 'Árak',
  historyEyebrow: 'Örökség',
  historyTitle: 'Történet címe',
  historyText: 'Történet szövege',
  galleryTitle: 'Képgaléria',
  galleryHint: 'Válasszon képet.',
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

const previewDetail = {
  id: content[0].id,
  slug: content[0].slug,
  name: hu.name,
  shortDescription: hu.shortDescription,
  roomCount: 5,
  coverImage: { path: '/cover.jpg', altText: 'Panzió', cover: true },
  description: hu.description,
  roomDescription: hu.roomDescription,
  pageText: {
    storyEyebrow: hu.storyEyebrow,
    storyTitle: hu.storyTitle,
    diningEyebrow: hu.diningEyebrow,
    diningTitle: hu.diningTitle,
    diningDescription: hu.diningDescription,
    amenitiesTitle: hu.amenitiesTitle,
    roomTypesTitle: hu.roomTypesTitle,
    pricingTitle: hu.pricingTitle,
    historyEyebrow: hu.historyEyebrow,
    galleryTitle: hu.galleryTitle,
    galleryHint: hu.galleryHint,
  },
  images: [
    { path: '/cover.jpg', altText: 'Panzió', cover: true },
    { path: '/story.jpg', altText: 'Udvar', cover: false },
  ],
  history: { title: hu.historyTitle, text: hu.historyText },
  contacts: [],
  address: { formatted: 'Csernakeresztúr', latitude: 45.8, longitude: 22.9 },
  roomTypes: [
    {
      id: 'double',
      name: 'Kétágyas szoba',
      quantity: 3,
      standardOccupancy: 2,
      roomsWithExtraBed: 0,
      extraBedsPerEligibleRoom: 0,
      features: [],
    },
  ],
  amenities: [],
  pricing: {
    currency: 'RON',
    items: [],
    taxes: [],
    surcharges: [],
    discounts: [],
    paymentNote: '',
  },
}

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

beforeEach(() => {
  vi.stubGlobal(
    'fetch',
    vi.fn(() =>
      Promise.resolve({ ok: true, status: 200, json: async () => previewDetail } as Response),
    ),
  )
})

afterEach(() => vi.restoreAllMocks())

describe('AdminGuesthouseContentEditor', () => {
  it('updates the amenities preview from the editable admin catalogue', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(response(200, content))
      .mockResolvedValueOnce(
        response(200, [
          {
            id: '9aff65ca-e42c-4d25-91d2-f6318a943be4',
            code: 'table_tennis',
            category: 'PROGRAM_GROUP',
            pricingType: 'FREE',
            translations: [
              {
                language: 'hu',
                name: 'Asztalitenisz',
                description: 'Ingyenes játék',
                detailedDescription: '',
              },
              { language: 'ro', name: '', description: '', detailedDescription: '' },
              { language: 'en', name: '', description: '', detailedDescription: '' },
            ],
            assignments: [
              { guesthouseId: content[0].id, active: true, displayOrder: 0 },
            ],
          },
        ]),
      )
    renderEditor(authorizedFetch)

    await user.click(await screen.findByRole('button', { name: 'Szolgáltatások szerkesztése' }))

    expect(await screen.findAllByText('Asztalitenisz')).toHaveLength(2)
    expect(screen.getByText('Ingyenes játék')).toBeVisible()
  })

  it('updates the live page preview and opens fields from the selected section', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi.fn().mockResolvedValue(response(200, content))
    renderEditor(authorizedFetch)

    const name = await screen.findByLabelText('Panzió neve')
    await screen.findByRole('heading', { name: 'Nisztor Panzió' })
    await user.clear(name)
    await user.type(name, 'Új élő név')

    expect(screen.getByRole('heading', { name: 'Új élő név' })).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Szobák szerkesztése' }))
    expect(screen.getByLabelText('Szobák bevezető szövege')).toBeVisible()
    expect(screen.queryByLabelText('Panzió neve')).not.toBeInTheDocument()
  })

  it('scrolls the preview viewport to the selected section', async () => {
    const user = userEvent.setup()
    const scrollTo = vi.fn()
    const originalScrollTo = Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'scrollTo')
    Object.defineProperty(HTMLElement.prototype, 'scrollTo', {
      configurable: true,
      value: scrollTo,
    })

    try {
      renderEditor(vi.fn().mockResolvedValue(response(200, content)))

      await user.click(await screen.findByRole('button', { name: /^Árak$/ }))

      await waitFor(() =>
        expect(scrollTo).toHaveBeenCalledWith(
          expect.objectContaining({ behavior: 'smooth', top: expect.any(Number) }),
        ),
      )
    } finally {
      if (originalScrollTo) Object.defineProperty(HTMLElement.prototype, 'scrollTo', originalScrollTo)
      else delete (HTMLElement.prototype as { scrollTo?: unknown }).scrollTo
    }
  })

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
      version: 0,
      name: 'Új Nisztor név',
    })
    expect(JSON.parse(request.body as string)).not.toHaveProperty('language')
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

  it('uses the branded dialog before leaving a page with unsaved changes', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi.fn().mockResolvedValue(response(200, content))
    const router = renderEditor(authorizedFetch)

    const name = await screen.findByLabelText('Panzió neve')
    await user.clear(name)
    await user.type(name, 'Módosított név')
    await router.navigate('/admin/bookings')

    expect(await screen.findByRole('heading', { name: 'Elveti a módosításokat?' })).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Maradok az oldalon' }))
    expect(router.state.location.pathname).toBe('/admin/content')

    await router.navigate('/admin/bookings')
    await user.click(await screen.findByRole('button', { name: 'Kilépés mentés nélkül' }))
    await waitFor(() => expect(router.state.location.pathname).toBe('/admin/bookings'))
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
