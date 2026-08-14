import { render, screen } from '@testing-library/react'
import { afterEach, describe, expect, it } from 'vitest'
import i18n from '../../i18n/config'
import GuesthouseAmenities from './GuesthouseAmenities'

const amenities = [
  {
    id: 'wifi',
    name: 'Wi-Fi',
    category: 'ROOM_COMFORT' as const,
    pricingType: 'FREE' as const,
  },
  {
    id: 'hot-tub',
    name: 'Dézsa',
    category: 'OUTDOOR_WELLNESS' as const,
    pricingType: 'PAID' as const,
  },
]

describe('GuesthouseAmenities', () => {
  afterEach(async () => {
    await i18n.changeLanguage('hu')
  })

  it.each([
    ['hu', 'Fizetős'],
    ['ro', 'Contra cost'],
    ['en', 'Paid'],
  ])('labels paid services in %s', async (language, label) => {
    await i18n.changeLanguage(language)

    render(<GuesthouseAmenities amenities={amenities} title="Szolgáltatások" />)

    expect(screen.getByText(label)).toBeVisible()
    expect(document.querySelectorAll('.amenity-paid-tag')).toHaveLength(1)
  })

  it('keeps the saved order inside each category', () => {
    render(
      <GuesthouseAmenities
        title="Szolgáltatások"
        amenities={[
          { id: 'television', name: 'Televízió', category: 'ROOM_COMFORT', pricingType: 'FREE', displayOrder: 4 },
          { id: 'wifi', name: 'Wi-Fi', category: 'ROOM_COMFORT', pricingType: 'FREE', displayOrder: 1 },
          { id: 'badminton', name: 'Tollaslabda', category: 'PROGRAM_GROUP', pricingType: 'FREE', displayOrder: 0 },
        ]}
      />,
    )

    const roomComfort = screen.getByRole('heading', { name: 'Szobai kényelem' }).closest('.amenity-group')
    expect([...roomComfort!.querySelectorAll('.amenity-name-row strong')].map((item) => item.textContent)).toEqual([
      'Wi-Fi',
      'Televízió',
    ])
    expect(screen.getByRole('heading', { name: 'Programok és csoportok' })).toBeVisible()
  })
})
