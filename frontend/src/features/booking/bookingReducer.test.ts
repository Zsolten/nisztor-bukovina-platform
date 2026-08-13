import { beforeEach, describe, expect, it } from 'vitest'
import {
  BOOKING_FLOW_STORAGE_KEY,
  bookingReducer,
  initialBookingFlowState,
  persistBookingFlowState,
  restoreBookingFlowState,
} from './bookingReducer'

describe('bookingReducer', () => {
  beforeEach(() => window.sessionStorage.clear())

  it('keeps the selected guesthouse id and slug in the flow state', () => {
    const selected = bookingReducer(initialBookingFlowState, {
      type: 'guesthouseSelected',
      guesthouseId: 'guesthouse-id',
      guesthouseSlug: 'nisztor-panzio',
    })

    expect(selected).toEqual({
      ...initialBookingFlowState,
      guesthouseId: 'guesthouse-id',
      guesthouseSlug: 'nisztor-panzio',
    })
  })

  it('keeps stay data when changing guesthouse and clears only room allocation', () => {
    const state = {
      ...initialBookingFlowState,
      guesthouseId: 'first-id',
      guesthouseSlug: 'nisztor-panzio',
      checkInDate: '2030-08-21',
      checkOutDate: '2030-08-24',
      adults: 2,
      childrenAge3to10: 1,
      roomQuantities: { double: 1, single: 1 },
    }

    const changed = bookingReducer(state, {
      type: 'guesthouseSelected',
      guesthouseId: 'second-id',
      guesthouseSlug: 'bukovina-panzio',
    })

    expect(changed.checkInDate).toBe('2030-08-21')
    expect(changed.checkOutDate).toBe('2030-08-24')
    expect(changed.adults).toBe(2)
    expect(changed.childrenAge3to10).toBe(1)
    expect(changed.roomQuantities).toEqual({})
  })

  it('persists booking details but excludes personal contact data', () => {
    const state = {
      ...initialBookingFlowState,
      guesthouseId: 'guesthouse-id',
      guesthouseSlug: 'nisztor-panzio',
      checkInDate: '2030-08-21',
      checkOutDate: '2030-08-24',
      adults: 3,
      childrenAge0to3: 1,
      roomQuantities: { triple: 1, single: 1 },
      contact: {
        contactName: 'Nisztor Zsolt',
        contactEmail: 'zsolt@example.com',
        contactPhone: '+40743677812',
        preferredLanguage: 'hu' as const,
        note: 'Csendes szobát kérünk.',
      },
      preferredLanguageSelectedByVisitor: true,
    }

    persistBookingFlowState(state)

    const stored = window.sessionStorage.getItem(BOOKING_FLOW_STORAGE_KEY)
    expect(stored).not.toBeNull()
    expect(stored).not.toContain('Nisztor Zsolt')
    expect(stored).not.toContain('zsolt@example.com')
    expect(stored).not.toContain('+40743677812')
    expect(stored).not.toContain('Csendes szobát kérünk.')
    expect(JSON.parse(stored ?? '{}')).not.toHaveProperty('contact')

    expect(restoreBookingFlowState()).toEqual({
      ...state,
      contact: initialBookingFlowState.contact,
      preferredLanguageSelectedByVisitor: false,
    })
  })

  it('discards personal data from a previously stored booking flow', () => {
    window.sessionStorage.setItem(
      BOOKING_FLOW_STORAGE_KEY,
      JSON.stringify({
        ...initialBookingFlowState,
        checkInDate: '2030-08-21',
        contact: {
          contactName: 'Nisztor Zsolt',
          contactEmail: 'zsolt@example.com',
          contactPhone: '+40743677812',
          preferredLanguage: 'hu',
          note: 'Csendes szobát kérünk.',
        },
        preferredLanguageSelectedByVisitor: true,
      }),
    )

    expect(restoreBookingFlowState()).toMatchObject({
      checkInDate: '2030-08-21',
      contact: initialBookingFlowState.contact,
      preferredLanguageSelectedByVisitor: false,
    })
  })

  it('never stores negative counts and clamps meal participants to the new guest total', () => {
    const state = {
      ...initialBookingFlowState,
      adults: 2,
      breakfastParticipants: 2,
      dinnerParticipants: 2,
    }

    const reduced = bookingReducer(state, {
      type: 'guestCountChanged',
      field: 'adults',
      value: 1,
    })
    const nonNegative = bookingReducer(reduced, {
      type: 'guestCountChanged',
      field: 'adults',
      value: -1,
    })

    expect(reduced.breakfastParticipants).toBe(1)
    expect(reduced.dinnerParticipants).toBe(1)
    expect(nonNegative.adults).toBe(0)
  })

  it('marks the contact language as visitor-selected only after an explicit selection', () => {
    const defaulted = bookingReducer(initialBookingFlowState, {
      type: 'contactChanged',
      field: 'preferredLanguage',
      value: 'ro',
    })
    const selected = bookingReducer(defaulted, {
      type: 'preferredLanguageSelected',
      value: 'en',
    })

    expect(defaulted.preferredLanguageSelectedByVisitor).toBe(false)
    expect(selected.contact.preferredLanguage).toBe('en')
    expect(selected.preferredLanguageSelectedByVisitor).toBe(true)
  })
})
