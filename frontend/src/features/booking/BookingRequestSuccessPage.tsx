import { CheckCircle2, Copy, Mail, Phone } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, Navigate, useLocation, useOutletContext } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import type { Language } from '../../i18n/languages'
import type { BookingRequestCreated } from '../../shared/api/bookings'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'
import type { BookingFlowState } from './bookingReducer'

interface BookingSuccessNavigationState {
  booking: BookingRequestCreated
  guesthouse: GuesthouseDetail
  bookingState: BookingFlowState
}

const LOCALES: Record<Language, string> = { hu: 'hu-HU', ro: 'ro-RO', en: 'en-GB' }

function isSuccessNavigationState(value: unknown): value is BookingSuccessNavigationState {
  if (!value || typeof value !== 'object') return false
  const state = value as Partial<BookingSuccessNavigationState>
  return Boolean(
    state.booking?.reference &&
    state.booking?.requestOnly &&
    state.guesthouse?.name &&
    state.bookingState,
  )
}

function preferredContact(guesthouse: GuesthouseDetail, type: 'PHONE' | 'EMAIL') {
  const contacts = guesthouse.contacts.filter((contact) => contact.type === type)
  return contacts.find((contact) => contact.preferred) ?? contacts[0] ?? null
}

export default function BookingRequestSuccessPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const { state: navigationState } = useLocation()
  const [copyState, setCopyState] = useState<'idle' | 'copied' | 'unavailable'>('idle')

  if (!isSuccessNavigationState(navigationState)) {
    return <Navigate to={`/${language}/booking`} replace />
  }

  const { booking, bookingState, guesthouse } = navigationState
  const phone = preferredContact(guesthouse, 'PHONE')
  const email = preferredContact(guesthouse, 'EMAIL')
  const selectedRooms = guesthouse.roomTypes
    .map((roomType) => ({ roomType, quantity: bookingState.roomQuantities[roomType.id] ?? 0 }))
    .filter(({ quantity }) => quantity > 0)
  const formatDate = (date: string) =>
    new Intl.DateTimeFormat(LOCALES[language], {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date(`${date}T00:00:00`))
  const total = new Intl.NumberFormat(LOCALES[language], {
    style: 'currency',
    currency: booking.currency,
    maximumFractionDigits: 0,
  }).format(booking.totalPayable)

  async function copyReference() {
    try {
      if (!navigator.clipboard?.writeText) throw new Error('Clipboard unavailable')
      await navigator.clipboard.writeText(booking.reference)
      setCopyState('copied')
    } catch {
      setCopyState('unavailable')
    }
  }

  return (
    <main id="main-content" className="booking-page booking-success-page">
      <section className="booking-success-card" aria-labelledby="booking-success-heading">
        <CheckCircle2 aria-hidden="true" size={42} />
        <p>{t('booking.requestReceivedEyebrow')}</p>
        <h1 id="booking-success-heading">{t('booking.requestReceivedTitle')}</h1>

        {(phone || email) && (
          <section
            className="booking-success-contact"
            aria-labelledby="booking-success-contact-heading"
          >
            <h2 id="booking-success-contact-heading">{t('booking.guesthouseContact')}</h2>
            {phone && (
              <a href={`tel:${phone.value}`}>
                <Phone aria-hidden="true" size={16} />
                {phone.value}
              </a>
            )}
            {email && (
              <a href={`mailto:${email.value}`}>
                <Mail aria-hidden="true" size={16} />
                {email.value}
              </a>
            )}
          </section>
        )}

        <div className="booking-success-facts">
          <article>
            <strong>{t('booking.successRequestSentTitle')}</strong>
            <span>{t('booking.successRequestSentMessage')}</span>
          </article>
          <article>
            <strong>{t('booking.successEmailTitle')}</strong>
            <span>{t('booking.successEmailMessage')}</span>
          </article>
          <article>
            <strong>{t('booking.successApprovalTitle')}</strong>
            <span>{t('booking.successApprovalMessage')}</span>
          </article>
        </div>

        <div className="booking-reference">
          <span>{t('booking.reference')}</span>
          <strong>{booking.reference}</strong>
          <button type="button" onClick={() => void copyReference()}>
            <Copy aria-hidden="true" size={16} />
            {t('booking.copyReference')}
          </button>
          {copyState === 'copied' && <small role="status">{t('booking.referenceCopied')}</small>}
          {copyState === 'unavailable' && (
            <small role="status">{t('booking.referenceCopyUnavailable')}</small>
          )}
        </div>

        <section
          className="booking-success-summary"
          aria-labelledby="booking-success-summary-heading"
        >
          <h2 id="booking-success-summary-heading">{t('booking.reviewSummary')}</h2>
          <dl>
            <div>
              <dt>{t('booking.guesthouse')}</dt>
              <dd>{guesthouse.name}</dd>
            </div>
            <div>
              <dt>{t('booking.arrival')}</dt>
              <dd>{formatDate(bookingState.checkInDate)}</dd>
            </div>
            <div>
              <dt>{t('booking.departure')}</dt>
              <dd>{formatDate(bookingState.checkOutDate)}</dd>
            </div>
            <div>
              <dt>{t('booking.nights')}</dt>
              <dd>{t('booking.nightTotal', { count: booking.nights })}</dd>
            </div>
            <div>
              <dt>{t('booking.guests')}</dt>
              <dd>{t('booking.guestTotal', { count: booking.totalGuests })}</dd>
            </div>
            <div>
              <dt>{t('booking.selectedRooms')}</dt>
              <dd>
                {selectedRooms
                  .map(({ roomType, quantity }) => `${quantity} × ${roomType.name}`)
                  .join(', ')}
              </dd>
            </div>
            <div>
              <dt>{t('booking.status')}</dt>
              <dd>{t('booking.statusReceived')}</dd>
            </div>
            <div className="booking-success-total">
              <dt>{t('booking.total')}</dt>
              <dd>{total}</dd>
            </div>
          </dl>
        </section>

        <p className="booking-success-payment">{t('booking.paymentOnSite')}</p>
        <Link to={`/${language}`}>{t('booking.backToHome')}</Link>
      </section>
    </main>
  )
}
