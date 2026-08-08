import { describe, expect, it } from 'vitest'
import { bookingReducer, initialBookingFlowState } from './bookingReducer'

describe('bookingReducer', () => {
  it('keeps the selected guesthouse id and slug in the flow state', () => {
    const selected = bookingReducer(initialBookingFlowState, {
      type: 'guesthouseSelected',
      guesthouseId: 'guesthouse-id',
      guesthouseSlug: 'nisztor-panzio',
    })

    expect(selected).toEqual({
      guesthouseId: 'guesthouse-id',
      guesthouseSlug: 'nisztor-panzio',
    })
  })
})
