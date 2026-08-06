import { act, render } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AppProviders } from '../../app/providers'
import HomepageHero from './HomepageHero'

const EXPECTED_IMAGE_ORDER = [
  '/images/guesthouses/bukovina/gallery-01.jpg',
  '/images/guesthouses/amenities/amenity-01.jpg',
  '/images/guesthouses/nisztor/gallery-01.jpg',
  '/images/destinations/deva-citadel.jpg',
]

function activeImageSource() {
  return document.querySelector('.hero-slide-active')?.getAttribute('src')
}

describe('HomepageHero', () => {
  afterEach(() => vi.useRealTimers())

  it('shows the hero images in a fixed six-second sequence', () => {
    vi.useFakeTimers()
    render(
      <AppProviders>
        <HomepageHero />
      </AppProviders>,
    )

    expect(activeImageSource()).toBe(EXPECTED_IMAGE_ORDER[0])

    for (const expectedSource of EXPECTED_IMAGE_ORDER.slice(1)) {
      act(() => vi.advanceTimersByTime(6_000))
      expect(activeImageSource()).toBe(expectedSource)
    }

    act(() => vi.advanceTimersByTime(6_000))
    expect(activeImageSource()).toBe(EXPECTED_IMAGE_ORDER[0])
  })
})
