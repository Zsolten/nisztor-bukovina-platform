import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { AppProviders } from '../../app/providers'
import i18n from '../../i18n/config'
import type { GuesthouseImage } from '../../shared/api/guesthouses'
import GuesthouseGallery from './GuesthouseGallery'

const images: GuesthouseImage[] = [
  { path: '/first.jpg', altText: 'Első kép', cover: true },
  { path: '/second.jpg', altText: 'Második kép', cover: false },
]

const patternImages: GuesthouseImage[] = Array.from({ length: 8 }, (_, index) => ({
  path: `/gallery-${index + 1}.jpg`,
  altText: `${index + 1}. galériakép`,
  cover: index === 0,
}))

function renderWithI18n(component: React.ReactNode) {
  return render(<AppProviders>{component}</AppProviders>)
}

describe('GuesthouseGallery', () => {
  beforeEach(async () => {
    await i18n.changeLanguage('hu')
  })

  it('opens the selected image in a modal and navigates cyclically', async () => {
    const user = userEvent.setup()
    renderWithI18n(<GuesthouseGallery images={images} />)

    await user.click(screen.getByRole('button', { name: 'Első kép' }))
    expect(screen.getByRole('dialog', { name: 'Első kép' })).toBeVisible()

    await user.click(screen.getByRole('button', { name: 'Következő kép' }))
    expect(screen.getByText('2 / 2')).toBeVisible()

    await user.click(screen.getByRole('button', { name: 'Következő kép' }))
    expect(screen.getByText('1 / 2')).toBeVisible()
  })

  it('closes the modal with Escape', async () => {
    const user = userEvent.setup()
    renderWithI18n(<GuesthouseGallery images={images} />)

    await user.click(screen.getByRole('button', { name: 'Első kép' }))
    await user.keyboard('{Escape}')

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })

  it('closes when the modal backdrop is selected', async () => {
    const user = userEvent.setup()
    renderWithI18n(<GuesthouseGallery images={images} />)

    await user.click(screen.getByRole('button', { name: 'Első kép' }))
    await user.click(screen.getByRole('dialog', { name: 'Első kép' }))

    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })

  it('keeps API order while repeating the six-position layout pattern', async () => {
    const user = userEvent.setup()
    renderWithI18n(<GuesthouseGallery images={patternImages} />)

    const buttons = screen.getAllByRole('button')
    expect(buttons.map((button) => button.getAttribute('aria-label'))).toEqual(
      patternImages.map((image) => image.altText),
    )
    buttons.forEach((button, index) => {
      expect(button).toHaveClass('gallery-item', `gallery-item--pattern-${index % 6}`)
    })

    await user.click(buttons[6])
    expect(screen.getByRole('dialog', { name: '7. galériakép' })).toBeVisible()
    expect(screen.getByText('7 / 8')).toBeVisible()

    await user.click(screen.getByRole('button', { name: 'Következő kép' }))
    expect(screen.getByText('8 / 8')).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Következő kép' }))
    expect(screen.getByText('1 / 8')).toBeVisible()
    await user.click(screen.getByRole('button', { name: 'Előző kép' }))
    expect(screen.getByText('8 / 8')).toBeVisible()
  })
})
