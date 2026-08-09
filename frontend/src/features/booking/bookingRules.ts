import type { BookingFlowState } from './bookingReducer'

const MILLISECONDS_PER_DAY = 86_400_000

function dateParts(value: string) {
  const [year, month, day] = value.split('-').map(Number)
  if (!year || !month || !day) return null
  return { year, month, day }
}

export function todayIso(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function addDays(value: string, days: number) {
  const parts = dateParts(value)
  if (!parts) return ''
  const date = new Date(Date.UTC(parts.year, parts.month - 1, parts.day + days))
  return date.toISOString().slice(0, 10)
}

export function nightsBetween(checkInDate: string, checkOutDate: string) {
  const checkIn = dateParts(checkInDate)
  const checkOut = dateParts(checkOutDate)
  if (!checkIn || !checkOut) return 0

  const difference =
    Date.UTC(checkOut.year, checkOut.month - 1, checkOut.day) -
    Date.UTC(checkIn.year, checkIn.month - 1, checkIn.day)
  return Math.max(0, Math.round(difference / MILLISECONDS_PER_DAY))
}

export function hasValidStayRange(state: BookingFlowState, today = todayIso()) {
  return state.checkInDate >= today && nightsBetween(state.checkInDate, state.checkOutDate) > 0
}

export function totalGuests(state: BookingFlowState) {
  return state.adults + state.childrenAge3to10 + state.childrenAge0to3
}
