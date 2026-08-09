import { ArrowLeft, LoaderCircle, Send } from 'lucide-react'
import { useEffect, useMemo, useRef, useState, type Dispatch, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import type { Language } from '../../i18n/languages'
import {
  BookingApiError,
  fetchBookingQuote,
  submitBookingRequest,
  type BookingQuote,
  type BookingQuoteRequest,
  type BookingRequestCreated,
  type CreateBookingRequest,
} from '../../shared/api/bookings'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'
import type { BookingFlowAction, BookingFlowState } from './bookingReducer'
import {
  bookingErrorMessageKey,
  contactErrorsFromApi,
  validateBookingContact,
  type ContactField,
  type ContactValidationErrors,
} from './bookingContact'
import { nightsBetween } from './bookingRules'

interface BookingReviewStepProps {
  language: Language
  guesthouse: GuesthouseDetail
  state: BookingFlowState
  dispatch: Dispatch<BookingFlowAction>
  onBack: () => void
  onSubmitted: (booking: BookingRequestCreated) => void
}

type QuoteState =
  | { status: 'loading'; quote: null }
  | { status: 'success'; quote: BookingQuote }
  | { status: 'error'; quote: null }

const LOCALES: Record<Language, string> = {
  hu: 'hu-HU',
  ro: 'ro-RO',
  en: 'en-GB',
}

function createIdempotencyKey() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) return crypto.randomUUID()
  return `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

export default function BookingReviewStep({
  language,
  guesthouse,
  state,
  dispatch,
  onBack,
  onSubmitted,
}: BookingReviewStepProps) {
  const { t } = useTranslation()
  const [quoteState, setQuoteState] = useState<QuoteState>({ status: 'loading', quote: null })
  const [clientErrors, setClientErrors] = useState<ContactValidationErrors>({})
  const [submissionError, setSubmissionError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [priceChanged, setPriceChanged] = useState(false)
  const [priceChangeConfirmed, setPriceChangeConfirmed] = useState(false)
  const idempotencyRef = useRef<{ signature: string; key: string } | null>(null)
  const submissionInFlightRef = useRef(false)

  const selectedRooms = useMemo(
    () =>
      guesthouse.roomTypes
        .map((roomType) => ({
          roomType,
          quantity: state.roomQuantities[roomType.id] ?? 0,
        }))
        .filter(({ quantity }) => quantity > 0),
    [guesthouse.roomTypes, state.roomQuantities],
  )
  const nights = nightsBetween(state.checkInDate, state.checkOutDate)
  const quoteRequest = useMemo<BookingQuoteRequest>(
    () => ({
      guesthouseId: state.guesthouseId ?? '',
      checkInDate: state.checkInDate,
      checkOutDate: state.checkOutDate,
      adults: state.adults,
      childrenAge3to10: state.childrenAge3to10,
      childrenAge0to3: state.childrenAge0to3,
      roomSelections: selectedRooms.map(({ roomType, quantity }) => ({
        roomTypeId: roomType.id,
        quantity,
      })),
      services: {
        breakfastParticipants: state.breakfastParticipants,
        dinnerParticipants: state.dinnerParticipants,
      },
    }),
    [
      selectedRooms,
      state.adults,
      state.breakfastParticipants,
      state.checkInDate,
      state.checkOutDate,
      state.childrenAge0to3,
      state.childrenAge3to10,
      state.dinnerParticipants,
      state.guesthouseId,
    ],
  )

  useEffect(() => {
    if (!state.preferredLanguageSelectedByVisitor && state.contact.preferredLanguage !== language) {
      dispatch({ type: 'contactChanged', field: 'preferredLanguage', value: language })
    }
  }, [dispatch, language, state.contact.preferredLanguage, state.preferredLanguageSelectedByVisitor])

  useEffect(() => {
    const controller = new AbortController()
    void fetchBookingQuote(quoteRequest, controller.signal)
      .then((quote) => setQuoteState({ status: 'success', quote }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setQuoteState({ status: 'error', quote: null })
      })

    return () => controller.abort()
  }, [quoteRequest])

  const formatDate = (value: string) => {
    if (!value) return t('booking.notSelected')
    const [year, month, day] = value.split('-').map(Number)
    return new Intl.DateTimeFormat(LOCALES[language], {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).format(new Date(year, month - 1, day))
  }
  const formatMoney = (amount: number, currency = 'RON') =>
    new Intl.NumberFormat(LOCALES[language], {
      style: 'currency',
      currency,
      maximumFractionDigits: 0,
    }).format(amount)
  const errorMessage = (code: string) => t(`booking.errors.${bookingErrorMessageKey(code)}`)

  function updateContact(field: ContactField, value: string) {
    dispatch({ type: 'contactChanged', field, value })
    setClientErrors((current) => ({ ...current, [field]: undefined }))
    setSubmissionError(null)
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (submitting || submissionInFlightRef.current) return

    const validationErrors = validateBookingContact(state.contact)
    if (Object.keys(validationErrors).length > 0) {
      setClientErrors(validationErrors)
      setSubmissionError(t('booking.errors.contactFormInvalid'))
      return
    }
    if (quoteState.status !== 'success') {
      setSubmissionError(t('booking.errors.quoteRequired'))
      return
    }
    if (priceChanged && !priceChangeConfirmed) {
      setSubmissionError(t('booking.errors.priceChangeConfirmationRequired'))
      return
    }

    const request: CreateBookingRequest = {
      ...quoteRequest,
      ...state.contact,
      acceptedTotal: quoteState.quote.priceBreakdown.totalPayable,
    }
    const signature = JSON.stringify(request)
    if (idempotencyRef.current?.signature !== signature) {
      idempotencyRef.current = { signature, key: createIdempotencyKey() }
    }

    setSubmitting(true)
    submissionInFlightRef.current = true
    setSubmissionError(null)
    try {
      const created = await submitBookingRequest(request, idempotencyRef.current.key)
      onSubmitted(created)
    } catch (error: unknown) {
      if (error instanceof BookingApiError) {
        if (error.body.code === 'BOOKING_PRICE_CHANGED' && error.body.currentQuote) {
          setQuoteState({ status: 'success', quote: error.body.currentQuote })
          setPriceChanged(true)
          setPriceChangeConfirmed(false)
          idempotencyRef.current = null
          setSubmissionError(t('booking.errors.priceChanged'))
        } else {
          const fieldErrors = contactErrorsFromApi(error.body.errors)
          setClientErrors(fieldErrors)
          setSubmissionError(errorMessage(error.body.code))
        }
      } else {
        setSubmissionError(t('booking.errors.generic'))
      }
    } finally {
      submissionInFlightRef.current = false
      setSubmitting(false)
    }
  }

  const quote = quoteState.status === 'success' ? quoteState.quote : null

  return (
    <main id="main-content" className="booking-page booking-review-page">
      <header className="booking-build-header">
        <h1>{t('booking.reviewTitle')}</h1>
        <button type="button" className="booking-back-button" onClick={onBack}>
          <ArrowLeft aria-hidden="true" size={19} />
          {t('booking.backToBooking')}
        </button>
      </header>

      <form className="booking-review-layout" noValidate onSubmit={submit}>
        <section className="booking-contact-section" aria-labelledby="booking-contact-heading">
          <div className="booking-review-heading">
            <div>
              <h2 id="booking-contact-heading">{t('booking.contactDetails')}</h2>
              <p>{t('booking.contactDetailsHint')}</p>
            </div>
          </div>

          <div className="booking-contact-grid">
            <label>
              <span>{t('booking.contactName')}</span>
              <input
                value={state.contact.contactName}
                maxLength={160}
                autoComplete="name"
                aria-invalid={Boolean(clientErrors.contactName)}
                onChange={(event) => updateContact('contactName', event.target.value)}
              />
              {clientErrors.contactName && (
                <small role="alert">{errorMessage(clientErrors.contactName)}</small>
              )}
            </label>
            <label>
              <span>{t('booking.contactEmail')}</span>
              <input
                type="email"
                value={state.contact.contactEmail}
                maxLength={320}
                autoComplete="email"
                aria-invalid={Boolean(clientErrors.contactEmail)}
                onChange={(event) => updateContact('contactEmail', event.target.value)}
              />
              {clientErrors.contactEmail && (
                <small role="alert">{errorMessage(clientErrors.contactEmail)}</small>
              )}
            </label>
            <label>
              <span>{t('booking.contactPhone')}</span>
              <input
                type="tel"
                value={state.contact.contactPhone}
                maxLength={40}
                autoComplete="tel"
                aria-invalid={Boolean(clientErrors.contactPhone)}
                onChange={(event) => updateContact('contactPhone', event.target.value)}
              />
              {clientErrors.contactPhone && (
                <small role="alert">{errorMessage(clientErrors.contactPhone)}</small>
              )}
            </label>
            <label>
              <span>{t('booking.preferredLanguage')}</span>
              <select
                value={state.contact.preferredLanguage}
                aria-invalid={Boolean(clientErrors.preferredLanguage)}
                onChange={(event) => {
                  dispatch({ type: 'preferredLanguageSelected', value: event.target.value })
                  setClientErrors((current) => ({ ...current, preferredLanguage: undefined }))
                  setSubmissionError(null)
                }}
              >
                <option value="hu">{t('booking.languageHu')}</option>
                <option value="ro">{t('booking.languageRo')}</option>
                <option value="en">{t('booking.languageEn')}</option>
              </select>
              {clientErrors.preferredLanguage && (
                <small role="alert">{errorMessage(clientErrors.preferredLanguage)}</small>
              )}
            </label>
            <label className="booking-contact-note">
              <span>{t('booking.noteOptional')}</span>
              <textarea
                value={state.contact.note}
                maxLength={2000}
                rows={4}
                aria-invalid={Boolean(clientErrors.note)}
                onChange={(event) => updateContact('note', event.target.value)}
              />
              {clientErrors.note && <small role="alert">{errorMessage(clientErrors.note)}</small>}
            </label>
          </div>
        </section>

        <aside className="booking-review-summary" aria-labelledby="booking-review-heading">
          <div className="booking-review-heading">
            <div>
              <h2 id="booking-review-heading">{t('booking.reviewSummary')}</h2>
              <p>{guesthouse.name}</p>
            </div>
          </div>
          <dl className="booking-review-details">
            <div>
              <dt>{t('booking.arrival')}</dt>
              <dd>{formatDate(state.checkInDate)}</dd>
            </div>
            <div>
              <dt>{t('booking.departure')}</dt>
              <dd>{formatDate(state.checkOutDate)}</dd>
            </div>
            <div>
              <dt>{t('booking.nights')}</dt>
              <dd>{t('booking.nightTotal', { count: nights })}</dd>
            </div>
            <div>
              <dt>{t('booking.guests')}</dt>
              <dd className="booking-review-list">
                <span>
                  {t('booking.adults')}: {state.adults}
                </span>
                <span>
                  {t('booking.childrenAge3to10')}: {state.childrenAge3to10}
                </span>
                <span>
                  {t('booking.childrenAge0to3')}: {state.childrenAge0to3}
                </span>
              </dd>
            </div>
            <div>
              <dt>{t('booking.selectedRooms')}</dt>
              <dd>
                {selectedRooms
                  .map(({ roomType, quantity }) => `${quantity} x ${roomType.name}`)
                  .join(', ')}
              </dd>
            </div>
            <div>
              <dt>{t('booking.selectedMeals')}</dt>
              <dd className="booking-review-list">
                <span>
                  {t('booking.breakfast')}: {state.breakfastParticipants}
                </span>
                <span>
                  {t('booking.dinner')}: {state.dinnerParticipants}
                </span>
              </dd>
            </div>
          </dl>

          <div className="booking-review-contact">
            <strong>{t('booking.contactDetails')}</strong>
            <span>{state.contact.contactName}</span>
            <span>{state.contact.contactEmail}</span>
            <span>{state.contact.contactPhone}</span>
            {state.contact.note && <span>{state.contact.note}</span>}
          </div>

          <div className="booking-review-price" aria-live="polite">
            {quoteState.status === 'loading' && <p>{t('booking.quoteLoading')}</p>}
            {quoteState.status === 'error' && (
              <p className="booking-review-error">{t('booking.errors.quoteRequired')}</p>
            )}
            {quote && (
              <dl>
                <div>
                  <dt>{t('booking.accommodation')}</dt>
                  <dd>{formatMoney(quote.priceBreakdown.accommodationTotal, quote.currency)}</dd>
                </div>
                <div>
                  <dt>{t('booking.breakfast')}</dt>
                  <dd>{formatMoney(quote.priceBreakdown.breakfastTotal, quote.currency)}</dd>
                </div>
                <div>
                  <dt>{t('booking.dinner')}</dt>
                  <dd>{formatMoney(quote.priceBreakdown.dinnerTotal, quote.currency)}</dd>
                </div>
                <div className="booking-review-total">
                  <dt>{t('booking.total')}</dt>
                  <dd>{formatMoney(quote.priceBreakdown.totalPayable, quote.currency)}</dd>
                </div>
              </dl>
            )}
          </div>
        </aside>

        <section className="booking-request-confirmation" aria-labelledby="booking-request-heading">
          <h2 id="booking-request-heading">{t('booking.beforeSubmission')}</h2>
          <ul>
            <li>{t('booking.requestNotConfirmation')}</li>
            <li>{t('booking.availabilityConfirmedLater')}</li>
            <li>{t('booking.paymentOnSite')}</li>
          </ul>
          {priceChanged && (
            <label className="booking-price-change-confirmation">
              <input
                type="checkbox"
                checked={priceChangeConfirmed}
                onChange={(event) => setPriceChangeConfirmed(event.target.checked)}
              />
              <span>{t('booking.confirmUpdatedPrice')}</span>
            </label>
          )}
          {submissionError && (
            <p className="booking-submission-error" role="alert">
              {submissionError}
            </p>
          )}
          <button type="submit" disabled={submitting || quoteState.status !== 'success'}>
            {submitting ? (
              <LoaderCircle aria-hidden="true" className="booking-spinner" size={19} />
            ) : (
              <Send aria-hidden="true" size={19} />
            )}
            {submitting ? t('booking.submitting') : t('booking.submitRequest')}
          </button>
        </section>
      </form>
    </main>
  )
}
