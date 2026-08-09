export interface BookingFlowState {
  guesthouseId: string | null
  guesthouseSlug: string | null
  checkInDate: string
  checkOutDate: string
  adults: number
  childrenAge3to10: number
  childrenAge0to3: number
  roomQuantities: Record<string, number>
  breakfastParticipants: number
  dinnerParticipants: number
}

export const initialBookingFlowState: BookingFlowState = {
  guesthouseId: null,
  guesthouseSlug: null,
  checkInDate: '',
  checkOutDate: '',
  adults: 1,
  childrenAge3to10: 0,
  childrenAge0to3: 0,
  roomQuantities: {},
  breakfastParticipants: 0,
  dinnerParticipants: 0,
}

export type BookingFlowAction =
  | { type: 'guesthouseSelected'; guesthouseId: string; guesthouseSlug: string }
  | { type: 'guesthouseCleared' }
  | { type: 'dateChanged'; field: 'checkInDate' | 'checkOutDate'; value: string }
  | {
      type: 'guestCountChanged'
      field: 'adults' | 'childrenAge3to10' | 'childrenAge0to3'
      value: number
    }
  | { type: 'roomQuantityChanged'; roomTypeId: string; value: number }
  | {
      type: 'mealParticipantsChanged'
      field: 'breakfastParticipants' | 'dinnerParticipants'
      value: number
    }

export const BOOKING_FLOW_STORAGE_KEY = 'bukovina-booking-flow'

export function restoreBookingFlowState(): BookingFlowState {
  try {
    const saved = window.sessionStorage.getItem(BOOKING_FLOW_STORAGE_KEY)
    if (!saved) return initialBookingFlowState

    const parsed = JSON.parse(saved) as Partial<BookingFlowState>
    return {
      ...initialBookingFlowState,
      ...parsed,
      roomQuantities: parsed.roomQuantities ?? {},
    }
  } catch {
    return initialBookingFlowState
  }
}

export function persistBookingFlowState(state: BookingFlowState) {
  window.sessionStorage.setItem(BOOKING_FLOW_STORAGE_KEY, JSON.stringify(state))
}

export function bookingReducer(
  state: BookingFlowState,
  action: BookingFlowAction,
): BookingFlowState {
  switch (action.type) {
    case 'guesthouseSelected':
      return {
        ...state,
        guesthouseId: action.guesthouseId,
        guesthouseSlug: action.guesthouseSlug,
        roomQuantities: state.guesthouseId === action.guesthouseId ? state.roomQuantities : {},
      }
    case 'guesthouseCleared':
      return initialBookingFlowState
    case 'dateChanged':
      return { ...state, [action.field]: action.value }
    case 'guestCountChanged': {
      const value = Math.max(0, action.value)
      const nextState = { ...state, [action.field]: value }
      const nextTotal = nextState.adults + nextState.childrenAge3to10 + nextState.childrenAge0to3
      return {
        ...nextState,
        breakfastParticipants: Math.min(nextState.breakfastParticipants, nextTotal),
        dinnerParticipants: Math.min(nextState.dinnerParticipants, nextTotal),
      }
    }
    case 'roomQuantityChanged':
      return {
        ...state,
        roomQuantities: {
          ...state.roomQuantities,
          [action.roomTypeId]: Math.max(0, action.value),
        },
      }
    case 'mealParticipantsChanged':
      return { ...state, [action.field]: Math.max(0, action.value) }
  }
}
