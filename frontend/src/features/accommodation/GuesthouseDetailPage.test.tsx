import { render, screen } from '@testing-library/react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import { appRoutes } from '../../app/router'

const detail = {
  slug: 'nisztor-panzio',
  name: 'Nisztor Panzió',
  shortDescription: 'Csendes, családias szállás.',
  roomCount: 5,
  coverImage: { path: '/cover.jpg', altText: 'Panzió', cover: true },
  description: 'A Nisztor Panzió Csernakeresztúron várja vendégeit.',
  roomDescription: 'Öt kényelmes szoba várja a vendégeket.',
  images: [{ path: '/cover.jpg', altText: 'Panzió', cover: true }],
  history: {
    title: 'Bukovinai székely örökség Csernakeresztúron',
    text: 'A hagyományokat nemzedékről nemzedékre továbbadják.',
  },
  contacts: [
    { type: 'PHONE', value: '+40 743 677 812', label: 'Nisztor Attila', preferred: true },
    { type: 'EMAIL', value: 'nisztorpanzio@gmail.com', label: 'E-mail', preferred: false },
  ],
  address: {
    formatted: 'Bucovina utca 17., Csernakeresztúr',
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
      description: 'Internetkapcsolat',
      category: 'ROOM_COMFORT',
    },
    {
      id: 'breakfast',
      name: 'Reggeli',
      description: 'Kérésre',
      category: 'FOOD_KITCHEN',
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

describe('GuesthouseDetailPage', () => {
  beforeEach(() => {
    window.localStorage.clear()
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({ ok: true, status: 200, json: async () => detail } as Response),
      ),
    )
  })

  it('presents all public information without a booking action', async () => {
    const router = createMemoryRouter(appRoutes, {
      initialEntries: ['/hu/guesthouses/nisztor-panzio'],
    })

    render(
      <AppProviders>
        <RouterProvider router={router} />
      </AppProviders>,
    )

    expect(
      await screen.findByRole('heading', { name: 'Bukovinai székely örökség Csernakeresztúron' }),
    ).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Szobatípusok' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Szobai kényelem' })).toBeVisible()
    expect(screen.getByRole('heading', { name: 'Étkezés és konyha' })).toBeVisible()
    expect(screen.getByText('130 RON')).toBeVisible()
    expect(screen.getByText('1%')).toBeVisible()
    expect(screen.getAllByText('Bucovina utca 17., Csernakeresztúr')).toHaveLength(2)
    expect(screen.getByRole('link', { name: /Nisztor Attila/ })).toHaveAttribute(
      'href',
      'tel:+40743677812',
    )
    expect(screen.getByRole('link', { name: /nisztorpanzio@gmail.com/ })).toHaveAttribute(
      'href',
      'mailto:nisztorpanzio@gmail.com',
    )
    expect(screen.queryByRole('link', { name: /foglal|book|rezerv/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /foglal|book|rezerv/i })).not.toBeInTheDocument()
  })
})
