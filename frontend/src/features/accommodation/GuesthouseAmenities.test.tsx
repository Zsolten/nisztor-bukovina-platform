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
})
