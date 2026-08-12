import {
  BedDouble,
  CalendarDays,
  CheckCircle2,
  Mail,
  Phone,
  Utensils,
  Users,
  WalletCards,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useOutletContext, useParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import type { Language } from '../../i18n/languages'
import {
  BookingApiError,
  cancelManagedBooking,
  fetchBookingManagementSummary,
  type BookingManagementSummary,
} from '../../shared/api/bookings'

const LOCALES: Record<Language, string> = { hu: 'hu-HU', ro: 'ro-RO', en: 'en-GB' }

const STATUS_KEYS: Record<BookingManagementSummary['status'], string> = {
  RECEIVED: 'bookingManagement.statuses.received',
  UNDER_REVIEW: 'bookingManagement.statuses.underReview',
  CONFIRMED: 'bookingManagement.statuses.confirmed',
  REJECTED: 'bookingManagement.statuses.rejected',
  CANCELLED: 'bookingManagement.statuses.cancelled',
}

export default function BookingManagementPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { token } = useParams()
  const { t } = useTranslation()
  const [booking, setBooking] = useState<BookingManagementSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [invalidLink, setInvalidLink] = useState(false)
  const [confirming, setConfirming] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [cancelled, setCancelled] = useState(false)
  const [cancellationError, setCancellationError] = useState(false)

  useEffect(() => {
    let active = true
    void fetchBookingManagementSummary(token ?? '', language)
      .then((summary) => {
        if (!active) return
        setBooking(summary)
        setInvalidLink(false)
      })
      .catch(() => {
        if (active) setInvalidLink(true)
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [language, token])

  async function cancelBooking() {
    if (!token || submitting) return
    setSubmitting(true)
    setCancellationError(false)
    try {
      await cancelManagedBooking(token)
      setCancelled(true)
      setConfirming(false)
    } catch (error) {
      if (error instanceof BookingApiError && error.status === 409) {
        try {
          setBooking(await fetchBookingManagementSummary(token, language))
        } catch {
          setInvalidLink(true)
        }
        setConfirming(false)
      } else {
        setCancellationError(true)
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <main id="main-content" className="booking-management-page">
        <p className="booking-management-state" role="status">
          {t('bookingManagement.loading')}
        </p>
      </main>
    )
  }

  if (invalidLink || !booking) {
    return (
      <main id="main-content" className="booking-management-page">
        <section className="booking-management-state">
          <h1>{t('bookingManagement.invalidTitle')}</h1>
          <p>{t('bookingManagement.invalidMessage')}</p>
          <Link to={`/${language}`}>{t('bookingManagement.backHome')}</Link>
        </section>
      </main>
    )
  }

  if (cancelled) {
    return (
      <main id="main-content" className="booking-management-page">
        <section className="booking-management-state booking-management-success">
          <CheckCircle2 aria-hidden="true" size={42} />
          <h1>{t('bookingManagement.cancelledTitle')}</h1>
          <p>{t('bookingManagement.cancelledMessage')}</p>
          <strong>{booking.reference}</strong>
          <Link to={`/${language}`}>{t('bookingManagement.backHome')}</Link>
        </section>
      </main>
    )
  }

  const date = (value: string) =>
    new Intl.DateTimeFormat(LOCALES[language], {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date(`${value}T00:00:00`))
  const money = (value: number) =>
    new Intl.NumberFormat(LOCALES[language], {
      style: 'currency',
      currency: booking.price.currency,
      maximumFractionDigits: 0,
    }).format(value)
  const totalGuests =
    booking.guests.adults + booking.guests.childrenAge3to10 + booking.guests.childrenAge0to3
  const phone = booking.guesthouse.contacts.find((contact) => contact.type === 'PHONE')
  const email = booking.guesthouse.contacts.find((contact) => contact.type === 'EMAIL')

  return (
    <main id="main-content" className="booking-management-page">
      <header className="booking-management-heading">
        <p>{t('bookingManagement.eyebrow')}</p>
        <h1>{t('bookingManagement.title')}</h1>
        <div>
          <span>{booking.reference}</span>
          <strong data-status={booking.status}>{t(STATUS_KEYS[booking.status])}</strong>
        </div>
      </header>

      <div className="booking-management-layout">
        <div className="booking-management-summary">
          <section aria-labelledby="management-stay-heading">
            <h2 id="management-stay-heading">{t('bookingManagement.stay')}</h2>
            <div className="booking-management-facts">
              <article>
                <CalendarDays aria-hidden="true" />
                <span>{t('bookingManagement.arrival')}</span>
                <strong>{date(booking.stay.checkInDate)}</strong>
              </article>
              <article>
                <CalendarDays aria-hidden="true" />
                <span>{t('bookingManagement.departure')}</span>
                <strong>{date(booking.stay.checkOutDate)}</strong>
              </article>
              <article>
                <Users aria-hidden="true" />
                <span>{t('bookingManagement.guests')}</span>
                <strong>{t('bookingManagement.guestCount', { count: totalGuests })}</strong>
              </article>
              <article>
                <BedDouble aria-hidden="true" />
                <span>{t('bookingManagement.nights')}</span>
                <strong>{t('bookingManagement.nightCount', { count: booking.stay.nights })}</strong>
              </article>
            </div>
          </section>

          <section
            className="booking-management-details"
            aria-labelledby="management-details-heading"
          >
            <h2 id="management-details-heading">{t('bookingManagement.details')}</h2>
            <div>
              <BedDouble aria-hidden="true" />
              <strong>{t('bookingManagement.rooms')}</strong>
              <span>
                {booking.rooms.map((room) => `${room.quantity} × ${room.name}`).join(', ')}
              </span>
            </div>
            <div>
              <Users aria-hidden="true" />
              <strong>{t('bookingManagement.guestComposition')}</strong>
              <span>
                {t('bookingManagement.guestCompositionValue', {
                  adults: booking.guests.adults,
                  older: booking.guests.childrenAge3to10,
                  younger: booking.guests.childrenAge0to3,
                })}
              </span>
            </div>
            <div>
              <Utensils aria-hidden="true" />
              <strong>{t('bookingManagement.meals')}</strong>
              <span>
                {t('bookingManagement.mealValue', {
                  breakfast: booking.services.breakfastParticipants,
                  dinner: booking.services.dinnerParticipants,
                })}
              </span>
            </div>
          </section>
        </div>

        <aside className="booking-management-action" aria-labelledby="management-action-heading">
          <p>{booking.guesthouse.name}</p>
          <h2 id="management-action-heading">{t('bookingManagement.total')}</h2>
          <strong className="booking-management-total">{money(booking.price.totalPayable)}</strong>
          <dl>
            <div>
              <dt>{t('bookingManagement.accommodation')}</dt>
              <dd>{money(booking.price.accommodationTotal)}</dd>
            </div>
            <div>
              <dt>{t('bookingManagement.breakfast')}</dt>
              <dd>{money(booking.price.breakfastTotal)}</dd>
            </div>
            <div>
              <dt>{t('bookingManagement.dinner')}</dt>
              <dd>{money(booking.price.dinnerTotal)}</dd>
            </div>
          </dl>

          {booking.status === 'CONFIRMED' ? (
            <section className="booking-management-contact">
              <h3>{t('bookingManagement.directContactTitle')}</h3>
              <p>{t('bookingManagement.directContactMessage')}</p>
              {phone && (
                <a href={`tel:${phone.value}`}>
                  <Phone aria-hidden="true" /> {phone.value}
                </a>
              )}
              {email && (
                <a href={`mailto:${email.value}`}>
                  <Mail aria-hidden="true" /> {email.value}
                </a>
              )}
            </section>
          ) : booking.cancellationAllowed ? (
            <div className="booking-management-cancellation">
              {!confirming ? (
                <button type="button" onClick={() => setConfirming(true)}>
                  {t('bookingManagement.cancelAction')}
                </button>
              ) : (
                <div role="alertdialog" aria-labelledby="cancel-confirmation-title">
                  <h3 id="cancel-confirmation-title">{t('bookingManagement.confirmTitle')}</h3>
                  <p>{t('bookingManagement.confirmMessage')}</p>
                  <div>
                    <button
                      type="button"
                      disabled={submitting}
                      onClick={() => void cancelBooking()}
                    >
                      {submitting
                        ? t('bookingManagement.cancelling')
                        : t('bookingManagement.confirmAction')}
                    </button>
                    <button
                      type="button"
                      disabled={submitting}
                      onClick={() => setConfirming(false)}
                    >
                      {t('bookingManagement.keepAction')}
                    </button>
                  </div>
                </div>
              )}
              {cancellationError && <p role="alert">{t('bookingManagement.cancelError')}</p>}
            </div>
          ) : (
            <p className="booking-management-closed">{t('bookingManagement.noAction')}</p>
          )}

          <p className="booking-management-payment">
            <WalletCards aria-hidden="true" /> {t('bookingManagement.paymentNotice')}
          </p>
        </aside>
      </div>
    </main>
  )
}
