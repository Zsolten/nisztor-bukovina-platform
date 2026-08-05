import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import AsyncStatus from './AsyncStatus'

describe('AsyncStatus', () => {
  it('announces loading without presenting an error', () => {
    render(<AsyncStatus variant="loading" message="Betöltés" />)

    expect(screen.getByRole('status')).toHaveTextContent('Betöltés')
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('presents failures as an alert', () => {
    render(<AsyncStatus variant="error" message="Hiba" />)

    expect(screen.getByRole('alert')).toHaveTextContent('Hiba')
  })
})
