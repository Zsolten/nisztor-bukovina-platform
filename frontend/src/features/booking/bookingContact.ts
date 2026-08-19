import type { BookingContactDetails, BookingFieldError } from '../../shared/api/bookings'

export const BOOKING_CONTACT_LIMITS = {
  contactName: 160,
  contactEmail: 320,
  contactPhone: 40,
  note: 2000,
} as const

const EMAIL_PATTERN = /^\S+@\S+\.\S+$/
const PHONE_PATTERN = /^\+?[0-9][0-9 ()-]{6,38}$/
const SUPPORTED_LANGUAGES = new Set(['hu', 'ro', 'en'])

export type ContactField = keyof BookingContactDetails
export type ContactValidationErrors = Partial<Record<ContactField, string>>

export function validateBookingContact(contact: BookingContactDetails): ContactValidationErrors {
  const errors: ContactValidationErrors = {}
  const name = contact.contactName.trim().replace(/\s+/g, ' ')
  const email = contact.contactEmail.trim()
  const phone = contact.contactPhone.trim()
  const language = contact.preferredLanguage.trim()
  const note = contact.note.trim()

  if (!name) errors.contactName = 'CONTACT_FIELD_REQUIRED'
  else if (name.length > BOOKING_CONTACT_LIMITS.contactName) errors.contactName = 'TEXT_TOO_LONG'

  if (!email) errors.contactEmail = 'CONTACT_FIELD_REQUIRED'
  else if (email.length > BOOKING_CONTACT_LIMITS.contactEmail) errors.contactEmail = 'TEXT_TOO_LONG'
  else if (!EMAIL_PATTERN.test(email)) errors.contactEmail = 'INVALID_EMAIL'

  if (!phone) errors.contactPhone = 'CONTACT_FIELD_REQUIRED'
  else if (phone.length > BOOKING_CONTACT_LIMITS.contactPhone) errors.contactPhone = 'TEXT_TOO_LONG'
  else if (!PHONE_PATTERN.test(phone)) errors.contactPhone = 'INVALID_PHONE'

  if (!language) errors.preferredLanguage = 'PREFERRED_LANGUAGE_REQUIRED'
  else if (!SUPPORTED_LANGUAGES.has(language)) errors.preferredLanguage = 'UNSUPPORTED_LANGUAGE'

  if (note.length > BOOKING_CONTACT_LIMITS.note) errors.note = 'TEXT_TOO_LONG'

  return errors
}

export function contactErrorsFromApi(errors: BookingFieldError[]): ContactValidationErrors {
  return errors.reduce<ContactValidationErrors>((result, error) => {
    if (
      error.field === 'contactName' ||
      error.field === 'contactEmail' ||
      error.field === 'contactPhone' ||
      error.field === 'preferredLanguage' ||
      error.field === 'note'
    ) {
      result[error.field] = error.code
    }
    return result
  }, {})
}

export const BOOKING_ERROR_MESSAGE_KEYS: Record<string, string> = {
  ACCEPTED_TOTAL_REQUIRED: 'acceptedTotalRequired',
  ADULT_REQUIRED: 'adultRequired',
  BOOKING_PRICE_CHANGED: 'priceChanged',
  BOOKING_RATE_LIMITED: 'rateLimited',
  BOOKING_VALIDATION_FAILED: 'invalidRequest',
  BOOKING_SERVICE_NOT_AVAILABLE: 'serviceNotAvailable',
  CHECK_IN_IN_PAST: 'checkInInPast',
  CHECK_IN_REQUIRED: 'checkInRequired',
  CHECK_OUT_REQUIRED: 'checkOutRequired',
  CONTACT_FIELD_REQUIRED: 'contactFieldRequired',
  DUPLICATE_ROOM_TYPE: 'duplicateRoomType',
  GUEST_COUNT_REQUIRED: 'guestCountRequired',
  GUEST_COUNT_TOO_LARGE: 'guestCountTooLarge',
  GUESTHOUSE_NOT_AVAILABLE: 'guesthouseNotAvailable',
  GUESTHOUSE_REQUIRED: 'guesthouseRequired',
  IDEMPOTENCY_KEY_REQUIRED: 'submissionRetry',
  IDEMPOTENCY_KEY_REUSED: 'submissionRetry',
  IDEMPOTENCY_KEY_TOO_LONG: 'submissionRetry',
  INSUFFICIENT_ROOM_CAPACITY: 'insufficientRoomCapacity',
  INVALID_ACCEPTED_TOTAL: 'acceptedTotalInvalid',
  INVALID_DATE_RANGE: 'invalidDateRange',
  INVALID_EMAIL: 'invalidEmail',
  INVALID_PHONE: 'invalidPhone',
  INVALID_REQUEST: 'invalidRequest',
  INVALID_ROOM_QUANTITY: 'invalidRoomQuantity',
  LARGE_GROUP_OFFLINE_ONLY: 'largeGroupOfflineOnly',
  MAX_ROOM_SELECTIONS: 'tooManyRoomSelections',
  NEGATIVE_GUEST_COUNT: 'guestCountRequired',
  NEGATIVE_SERVICE_PARTICIPANTS: 'serviceParticipantsExceedGuests',
  PREFERRED_LANGUAGE_REQUIRED: 'preferredLanguageRequired',
  REQUEST_REQUIRED: 'invalidRequest',
  ROOM_QUANTITY_EXCEEDS_STOCK: 'roomQuantityExceedsStock',
  ROOM_QUANTITY_REQUIRED: 'invalidRoomQuantity',
  ROOM_SELECTION_REQUIRED: 'roomSelectionRequired',
  ROOM_TYPE_GUESTHOUSE_MISMATCH: 'roomNotAvailable',
  ROOM_TYPE_NOT_BOOKABLE: 'roomNotAvailable',
  ROOM_TYPE_NOT_FOUND: 'roomNotAvailable',
  ROOM_TYPE_REQUIRED: 'roomNotAvailable',
  SERVICE_PARTICIPANTS_EXCEED_GUESTS: 'serviceParticipantsExceedGuests',
  TEXT_TOO_LONG: 'textTooLong',
  TOO_MANY_ROOM_SELECTIONS: 'tooManyRoomSelections',
  TOO_MANY_ROOMS: 'roomQuantityExceedsStock',
  TOO_MANY_SINGLE_ROOMS: 'roomQuantityExceedsStock',
  TOTAL_GUESTS_REQUIRED: 'guestCountRequired',
  UNSUPPORTED_LANGUAGE: 'unsupportedLanguage',
}

export function bookingErrorMessageKey(code: string) {
  return BOOKING_ERROR_MESSAGE_KEYS[code] ?? 'generic'
}
