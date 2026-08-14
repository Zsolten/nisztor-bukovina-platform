import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import type { AdminAmenity } from '../api/adminAmenities'
import type { AdminGuesthouseContent } from '../api/adminGuesthouseContent'
import { AdminAuthContext } from '../auth/adminAuthContext'
import AdminAmenityEditor from './AdminAmenityEditor'

const guesthouseId = '82b508e1-2893-4f45-8cc8-7a6f50b43a4d'

const guesthouses: AdminGuesthouseContent[] = [
  {
    id: guesthouseId,
    slug: 'nisztor-panzio',
    active: true,
    translations: [
      contentTranslation('hu', 'Nisztor Panzió'),
      contentTranslation('ro', 'Pensiunea Nisztor'),
      contentTranslation('en', 'Nisztor Guesthouse'),
    ],
  },
]

const existingAmenity: AdminAmenity = {
  id: '9aff65ca-e42c-4d25-91d2-f6318a943be4',
  code: 'table_tennis',
  category: 'PROGRAM_GROUP',
  pricingType: 'FREE',
  translations: [
    { language: 'hu', name: 'Asztalitenisz', description: '', detailedDescription: '' },
    { language: 'ro', name: '', description: '', detailedDescription: '' },
    { language: 'en', name: '', description: '', detailedDescription: '' },
  ],
  assignments: [{ guesthouseId, active: true, displayOrder: 0 }],
}

describe('AdminAmenityEditor', () => {
  it('creates a service from a non-Hungarian page language', async () => {
    const user = userEvent.setup()
    const onCatalogueChange = vi.fn()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(response(200, []))
      .mockResolvedValueOnce(
        response(201, {
          ...existingAmenity,
          id: '51d1d094-e0d6-4dd1-87fa-d1c9369bc4fb',
          code: 'tollaslabda',
          translations: existingAmenity.translations.map((translation) =>
            translation.language === 'hu' ? { ...translation, name: 'Tollaslabda' } : translation,
          ),
        }),
      )
    renderEditor(authorizedFetch, 'ro', onCatalogueChange)

    await user.click(await screen.findByRole('button', { name: 'Új szolgáltatás' }))
    await user.click(screen.getByRole('tab', { name: 'Magyar' }))
    await user.type(screen.getByLabelText(/Megnevezés/), 'Tollaslabda')

    expect(screen.getByLabelText('Azonosító kód')).toHaveValue('tollaslabda')
    await user.click(screen.getByRole('button', { name: 'Szolgáltatás mentése' }))

    await waitFor(() => expect(authorizedFetch).toHaveBeenCalledTimes(2))
    expect(authorizedFetch).toHaveBeenLastCalledWith(
      '/api/admin/amenities',
      expect.objectContaining({ method: 'POST' }),
    )
    expect(await screen.findByText('A szolgáltatás mentése sikerült.')).toBeVisible()
    expect(onCatalogueChange).toHaveBeenLastCalledWith([
      expect.objectContaining({ code: 'tollaslabda' }),
    ])
  })

  it('edits an existing service and exposes a failed save inside the dialog', async () => {
    const user = userEvent.setup()
    const authorizedFetch = vi
      .fn()
      .mockResolvedValueOnce(response(200, [existingAmenity]))
      .mockResolvedValueOnce(
        response(400, { code: 'ADMIN_AMENITY_VALIDATION_FAILED', fieldErrors: {} }),
      )
    renderEditor(authorizedFetch, 'hu')

    await user.click(await screen.findByRole('button', { name: 'Szerkesztés' }))
    const name = screen.getByLabelText(/Megnevezés/)
    await user.clear(name)
    await user.type(name, 'Pingpong')
    await user.click(screen.getByRole('button', { name: 'Szolgáltatás mentése' }))

    expect(
      await screen.findByText('Ellenőrizd a megadott szolgáltatásadatokat és a hozzárendelést.'),
    ).toBeVisible()
    expect(screen.getByRole('dialog')).toBeVisible()
    expect(authorizedFetch).toHaveBeenLastCalledWith(
      `/api/admin/amenities/${existingAmenity.id}`,
      expect.objectContaining({ method: 'PUT' }),
    )
  })
})

function renderEditor(
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>,
  language: 'hu' | 'ro' | 'en',
  onCatalogueChange?: (amenities: AdminAmenity[]) => void,
) {
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
      <AdminAmenityEditor
        guesthouses={guesthouses}
        language={language}
        onCatalogueChange={onCatalogueChange}
        selectedGuesthouseId={guesthouseId}
      />
    </AdminAuthContext>,
  )
}

function response(status: number, body: unknown) {
  return {
    json: async () => body,
    ok: status >= 200 && status < 300,
    status,
  } as Response
}

function contentTranslation(language: 'hu' | 'ro' | 'en', name: string) {
  return {
    language,
    version: 0,
    name,
    shortDescription: '',
    description: '',
    roomDescription: '',
    storyEyebrow: '',
    storyTitle: '',
    diningEyebrow: '',
    diningTitle: '',
    diningDescription: '',
    amenitiesTitle: '',
    roomTypesTitle: '',
    pricingTitle: '',
    historyEyebrow: '',
    historyTitle: '',
    historyText: '',
    galleryTitle: '',
    galleryHint: '',
  }
}
