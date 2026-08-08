export interface BookingFlowState {
  guesthouseId: string | null
  guesthouseSlug: string | null
}

export const initialBookingFlowState: BookingFlowState = {
  guesthouseId: null,
  guesthouseSlug: null,
}

export type BookingFlowAction =
  | { type: 'guesthouseSelected'; guesthouseId: string; guesthouseSlug: string }
  | { type: 'guesthouseCleared' }

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
      }
    case 'guesthouseCleared':
      return initialBookingFlowState
  }
}
