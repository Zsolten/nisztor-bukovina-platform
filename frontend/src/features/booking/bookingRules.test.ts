import { describe, expect, it } from 'vitest'
import { initialBookingFlowState } from './bookingReducer'
import { addDays, hasValidStayRange, nightsBetween, totalGuests } from './bookingRules'

describe('bookingRules', () => {
  it('derives nights and accepts only a future-increasing range', () => {
    expect(nightsBetween('2030-08-21', '2030-08-24')).toBe(3)
    expect(nightsBetween('2030-08-24', '2030-08-24')).toBe(0)
    expect(
      hasValidStayRange(
        {
          ...initialBookingFlowState,
          checkInDate: '2030-08-21',
          checkOutDate: '2030-08-24',
        },
        '2030-08-20',
      ),
    ).toBe(true)
    expect(
      hasValidStayRange(
        {
          ...initialBookingFlowState,
          checkInDate: '2030-08-19',
          checkOutDate: '2030-08-24',
        },
        '2030-08-20',
      ),
    ).toBe(false)
  })

  it('handles calendar-day arithmetic without timezone-dependent parsing', () => {
    expect(addDays('2030-12-31', 1)).toBe('2031-01-01')
  })

  it('sums all three guest categories', () => {
    expect(
      totalGuests({
        ...initialBookingFlowState,
        adults: 2,
        childrenAge3to10: 2,
        childrenAge0to3: 1,
      }),
    ).toBe(5)
  })
})
