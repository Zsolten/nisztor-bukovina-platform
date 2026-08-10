import { describe, expect, it } from 'vitest'
import { resources } from '../../i18n/resources'
import { BOOKING_ERROR_MESSAGE_KEYS, validateBookingContact } from './bookingContact'

const validContact = {
  contactName: 'Nisztor Zsolt',
  contactEmail: 'zsolt@example.com',
  contactPhone: '+40 743 677 812',
  preferredLanguage: 'hu' as const,
  note: '',
}

describe('booking contact validation', () => {
  it('matches the server contact constraints for required fields and formats', () => {
    expect(validateBookingContact(validContact)).toEqual({})
    expect(
      validateBookingContact({
        ...validContact,
        contactName: ' ',
        contactEmail: 'not-an-email',
        contactPhone: '123',
        preferredLanguage: '' as const,
      }),
    ).toEqual({
      contactName: 'CONTACT_FIELD_REQUIRED',
      contactEmail: 'INVALID_EMAIL',
      contactPhone: 'INVALID_PHONE',
      preferredLanguage: 'PREFERRED_LANGUAGE_REQUIRED',
    })
  })

  it('maps every booking error code exposed by the backend to a localized message key', () => {
    const backendCodes = [
      'ACCEPTED_TOTAL_REQUIRED',
      'ADULT_REQUIRED',
      'BOOKING_PRICE_CHANGED',
      'BOOKING_SERVICE_NOT_AVAILABLE',
      'BOOKING_VALIDATION_FAILED',
      'CHECK_IN_IN_PAST',
      'CHECK_IN_REQUIRED',
      'CHECK_OUT_REQUIRED',
      'CONTACT_FIELD_REQUIRED',
      'DUPLICATE_ROOM_TYPE',
      'GUEST_COUNT_REQUIRED',
      'GUEST_COUNT_TOO_LARGE',
      'GUESTHOUSE_NOT_AVAILABLE',
      'GUESTHOUSE_REQUIRED',
      'IDEMPOTENCY_KEY_REQUIRED',
      'IDEMPOTENCY_KEY_REUSED',
      'IDEMPOTENCY_KEY_TOO_LONG',
      'INSUFFICIENT_ROOM_CAPACITY',
      'INVALID_ACCEPTED_TOTAL',
      'INVALID_DATE_RANGE',
      'INVALID_EMAIL',
      'INVALID_PHONE',
      'INVALID_REQUEST',
      'INVALID_ROOM_QUANTITY',
      'LARGE_GROUP_OFFLINE_ONLY',
      'MAX_ROOM_SELECTIONS',
      'NEGATIVE_GUEST_COUNT',
      'NEGATIVE_SERVICE_PARTICIPANTS',
      'PREFERRED_LANGUAGE_REQUIRED',
      'REQUEST_REQUIRED',
      'ROOM_QUANTITY_EXCEEDS_STOCK',
      'ROOM_QUANTITY_REQUIRED',
      'ROOM_SELECTION_REQUIRED',
      'ROOM_TYPE_GUESTHOUSE_MISMATCH',
      'ROOM_TYPE_NOT_BOOKABLE',
      'ROOM_TYPE_NOT_FOUND',
      'ROOM_TYPE_REQUIRED',
      'SERVICE_PARTICIPANTS_EXCEED_GUESTS',
      'TEXT_TOO_LONG',
      'TOO_MANY_ROOM_SELECTIONS',
      'TOO_MANY_ROOMS',
      'TOO_MANY_SINGLE_ROOMS',
      'TOTAL_GUESTS_REQUIRED',
      'UNSUPPORTED_LANGUAGE',
    ]

    expect(backendCodes.every((code) => BOOKING_ERROR_MESSAGE_KEYS[code] !== undefined)).toBe(true)
    const messageKeys = new Set(Object.values(BOOKING_ERROR_MESSAGE_KEYS))
    for (const language of ['hu', 'ro', 'en'] as const) {
      const errors = resources[language].translation.booking.errors
      expect([...messageKeys].every((key) => errors[key as keyof typeof errors])).toBe(true)
    }
  })
})
