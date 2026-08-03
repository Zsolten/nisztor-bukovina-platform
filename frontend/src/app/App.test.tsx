import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import FoundationScreen from './FoundationScreen'

describe('FoundationScreen', () => {
  it('renders the foundation product name', () => {
    render(<FoundationScreen />)

    expect(screen.getByRole('heading', { name: 'Nisztor-Bukovina Platform' })).toBeInTheDocument()
  })
})
