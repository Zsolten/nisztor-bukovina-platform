import {
  ArrowLeft,
  BedDouble,
  CheckCircle2,
  Info,
  LockKeyhole,
  Minus,
  Moon,
  Phone,
  Plus,
  RefreshCw,
  Soup,
  Users,
} from 'lucide-react'
import { useEffect, useMemo, useState, type Dispatch } from 'react'
import { useTranslation } from 'react-i18next'
import type { Language } from '../../i18n/languages'
import {
  fetchBookingQuote,
  type BookingQuote,
  type BookingQuoteRequest,
} from '../../shared/api/bookings'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'
import type { BookingFlowAction, BookingFlowState } from './bookingReducer'
import { addDays, hasValidStayRange, nightsBetween, todayIso, totalGuests } from './bookingRules'

interface BookingStayStepProps {
  language: Language
  guesthouse: GuesthouseDetail
  state: BookingFlowState
  dispatch: Dispatch<BookingFlowAction>
  onBack: () => void
  onContinue?: () => void
}

interface NumericCounterProps {
  label: string
  value: number
  min?: number
  max: number
  onChange: (value: number) => void
}

type QuoteState =
  | { status: 'idle'; quote: null; requestKey: null }
  | { status: 'loading'; quote: BookingQuote | null; requestKey: string }
  | { status: 'success'; quote: BookingQuote; requestKey: string }
  | { status: 'error'; quote: BookingQuote | null; requestKey: string }

const LOCALES: Record<Language, string> = {
  hu: 'hu-HU',
  ro: 'ro-RO',
  en: 'en-GB',
}

// Keep aligned with booking.public.large-group-threshold in backend/src/main/resources/application.yaml.
const LARGE_GROUP_THRESHOLD = 30

function NumericCounter({ label, value, min = 0, max, onChange }: NumericCounterProps) {
  return (
    <div className="booking-counter">
      <button
        type="button"
        aria-label={`${label}: -1`}
        disabled={value <= min}
        onClick={() => onChange(value - 1)}
      >
        <Minus aria-hidden="true" size={18} />
      </button>
      <output aria-label={label}>{value}</output>
      <button
        type="button"
        aria-label={`${label}: +1`}
        disabled={value >= max}
        onClick={() => onChange(value + 1)}
      >
        <Plus aria-hidden="true" size={18} />
      </button>
    </div>
  )
}

export default function BookingStayStep({
  language,
  guesthouse,
  state,
  dispatch,
  onBack,
  onContinue,
}: BookingStayStepProps) {
  const { t } = useTranslation()
  const [quoteRetry, setQuoteRetry] = useState(0)
  const [quoteState, setQuoteState] = useState<QuoteState>({
    status: 'idle',
    quote: null,
    requestKey: null,
  })

  const today = todayIso()
  const nights = nightsBetween(state.checkInDate, state.checkOutDate)
  const guestTotal = totalGuests(state)
  const selectedRooms = useMemo(
    () =>
      guesthouse.roomTypes
        .map((roomType) => ({
          roomTypeId: roomType.id,
          quantity: state.roomQuantities[roomType.id] ?? 0,
          capacity: roomType.standardOccupancy,
        }))
        .filter((room) => room.quantity > 0),
    [guesthouse.roomTypes, state.roomQuantities],
  )
  const selectedRoomCount = selectedRooms.reduce((sum, room) => sum + room.quantity, 0)
  const selectedCapacity = selectedRooms.reduce(
    (sum, room) => sum + room.quantity * room.capacity,
    0,
  )
  const roomCapacityMatches = guestTotal > 0 && selectedCapacity === guestTotal
  const dateRangeValid = hasValidStayRange(state, today)
  const largeGroup = guestTotal > LARGE_GROUP_THRESHOLD
  const canRequestQuote =
    Boolean(state.guesthouseId) &&
    dateRangeValid &&
    state.adults > 0 &&
    roomCapacityMatches &&
    selectedRooms.length > 0 &&
    !largeGroup

  const quoteRequest = useMemo<BookingQuoteRequest | null>(() => {
    if (!canRequestQuote || !state.guesthouseId) return null

    return {
      guesthouseId: state.guesthouseId,
      checkInDate: state.checkInDate,
      checkOutDate: state.checkOutDate,
      adults: state.adults,
      childrenAge3to10: state.childrenAge3to10,
      childrenAge0to3: state.childrenAge0to3,
      roomSelections: selectedRooms.map(({ roomTypeId, quantity }) => ({
        roomTypeId,
        quantity,
      })),
      services: {
        breakfastParticipants: state.breakfastParticipants,
        dinnerParticipants: state.dinnerParticipants,
      },
    }
  }, [
    canRequestQuote,
    selectedRooms,
    state.adults,
    state.breakfastParticipants,
    state.checkInDate,
    state.checkOutDate,
    state.childrenAge0to3,
    state.childrenAge3to10,
    state.dinnerParticipants,
    state.guesthouseId,
  ])

  useEffect(() => {
    if (!quoteRequest) return

    const controller = new AbortController()
    const requestKey = JSON.stringify(quoteRequest)
    const timeout = window.setTimeout(() => {
      setQuoteState((current) => ({ status: 'loading', quote: current.quote, requestKey }))
      void fetchBookingQuote(quoteRequest, controller.signal)
        .then((quote) => setQuoteState({ status: 'success', quote, requestKey }))
        .catch((error: unknown) => {
          if (error instanceof DOMException && error.name === 'AbortError') return
          setQuoteState((current) => ({ status: 'error', quote: current.quote, requestKey }))
        })
    }, 400)

    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [quoteRequest, quoteRetry])

  const formatMoney = (amount: number, currency = 'RON') =>
    new Intl.NumberFormat(LOCALES[language], {
      style: 'currency',
      currency,
      maximumFractionDigits: 0,
    }).format(amount)

  const accommodationPrice = guesthouse.pricing.items.find((item) => item.id === 'accommodation')
  const singleRoomPrice = guesthouse.pricing.items.find((item) => item.id === 'single_room')
  const touristTaxPercentage = guesthouse.pricing.taxes[0]?.percentage
  const currentRequestKey = quoteRequest ? JSON.stringify(quoteRequest) : null
  const quoteIsCurrent = currentRequestKey !== null && quoteState.requestKey === currentRequestKey
  const quoteStatus = quoteIsCurrent ? quoteState.status : 'idle'
  const quote = quoteIsCurrent ? quoteState.quote : null
  const canContinue = quoteStatus === 'success' && !largeGroup

  function changeCheckIn(value: string) {
    dispatch({ type: 'dateChanged', field: 'checkInDate', value })
    if (state.checkOutDate && state.checkOutDate <= value) {
      dispatch({ type: 'dateChanged', field: 'checkOutDate', value: '' })
    }
  }

  return (
    <main id="main-content" className="booking-page booking-stay-page">
      <header className="booking-build-header">
        <h1>{t('booking.composeTitle')}</h1>
        <button type="button" className="booking-back-button" onClick={onBack}>
          <ArrowLeft aria-hidden="true" size={19} />
          {t('booking.chooseAnotherGuesthouse')}
        </button>
      </header>

      <form
        className="booking-stay-form"
        onSubmit={(event) => {
          event.preventDefault()
          if (canContinue) onContinue?.()
        }}
      >
        <section className="booking-stay-inputs" aria-labelledby="booking-stay-heading">
          <div className="booking-date-grid">
            <label>
              <span>{t('booking.arrival')}</span>
              <input
                type="date"
                value={state.checkInDate}
                min={today}
                onChange={(event) => changeCheckIn(event.target.value)}
              />
            </label>
            <label>
              <span>{t('booking.departure')}</span>
              <input
                type="date"
                value={state.checkOutDate}
                min={state.checkInDate ? addDays(state.checkInDate, 1) : addDays(today, 1)}
                disabled={!state.checkInDate}
                onChange={(event) =>
                  dispatch({
                    type: 'dateChanged',
                    field: 'checkOutDate',
                    value: event.target.value,
                  })
                }
              />
            </label>
            <div className="booking-readonly-value">
              <span>{t('booking.nights')}</span>
              <output>{t('booking.nightTotal', { count: nights })}</output>
            </div>
          </div>

          <div className="booking-guest-grid">
            {(
              [
                ['adults', 'booking.adults'],
                ['childrenAge3to10', 'booking.childrenAge3to10'],
                ['childrenAge0to3', 'booking.childrenAge0to3'],
              ] as const
            ).map(([field, labelKey]) => (
              <div className="booking-guest-row" key={field}>
                <span>{t(labelKey)}</span>
                <NumericCounter
                  label={t(labelKey)}
                  value={state[field]}
                  max={99}
                  onChange={(value) => dispatch({ type: 'guestCountChanged', field, value })}
                />
              </div>
            ))}
            <div className="booking-guest-total">
              <Users aria-hidden="true" />
              <span>{t('booking.totalGuests')}</span>
              <strong>{t('booking.guestTotal', { count: guestTotal })}</strong>
            </div>
          </div>

          {state.adults === 0 && guestTotal > 0 && (
            <p className="booking-validation-message">{t('booking.adultRequired')}</p>
          )}
          <p className="booking-child-pricing-note">{t('booking.childAccommodationNotice')}</p>
        </section>

        <section className="booking-room-section" aria-labelledby="booking-room-heading">
          <div className="booking-room-image">
            <img src={guesthouse.coverImage.path} alt={guesthouse.coverImage.altText} />
          </div>
          <div className="booking-room-options">
            <div className="booking-room-title">
              <div>
                <span>02</span>
                <h2 id="booking-room-heading">{guesthouse.name}</h2>
              </div>
              <small>{t('booking.numberOfRooms')}</small>
            </div>

            <div className="booking-room-list">
              {guesthouse.roomTypes
                .filter(
                  (roomType) => roomType.standardOccupancy >= 1 && roomType.standardOccupancy <= 4,
                )
                .map((roomType) => {
                  const quantity = state.roomQuantities[roomType.id] ?? 0
                  const roomPrice =
                    roomType.standardOccupancy === 1
                      ? singleRoomPrice?.amount
                      : accommodationPrice && accommodationPrice.amount * roomType.standardOccupancy
                  return (
                    <article className="booking-room-row" key={roomType.id}>
                      <div>
                        <h3>{roomType.name}</h3>
                        <p>
                          <Users aria-hidden="true" size={16} />
                          {t('booking.roomCapacity', { count: roomType.standardOccupancy })}
                        </p>
                      </div>
                      {roomPrice !== undefined && (
                        <p className="booking-room-price">
                          {formatMoney(roomPrice)}
                          <small>
                            {roomType.standardOccupancy === 1
                              ? t('booking.perRoomNight')
                              : t('booking.roomPriceDetail', {
                                  amount: formatMoney(accommodationPrice?.amount ?? 0),
                                })}
                          </small>
                        </p>
                      )}
                      <NumericCounter
                        label={`${roomType.name} – ${t('booking.numberOfRooms')}`}
                        value={quantity}
                        max={roomType.quantity}
                        onChange={(value) =>
                          dispatch({
                            type: 'roomQuantityChanged',
                            roomTypeId: roomType.id,
                            value,
                          })
                        }
                      />
                    </article>
                  )
                })}
            </div>

            <div
              className={`booking-capacity-status${roomCapacityMatches ? ' is-valid' : ''}`}
              aria-live="polite"
            >
              {roomCapacityMatches ? (
                <CheckCircle2 aria-hidden="true" />
              ) : (
                <Info aria-hidden="true" />
              )}
              <span>
                {t('booking.capacityStatus', {
                  guests: guestTotal,
                  capacity: selectedCapacity,
                })}
              </span>
            </div>
          </div>
        </section>

        <section className="booking-meals-section" aria-labelledby="booking-meals-heading">
          <div className="booking-section-heading">
            <span>03</span>
            <div>
              <h2 id="booking-meals-heading">{t('booking.meals')}</h2>
              <p>{t('booking.mealsHint')}</p>
            </div>
          </div>
          <div className="booking-meal-grid">
            <div className="booking-meal-option">
              <Soup aria-hidden="true" />
              <div>
                <strong>{t('booking.breakfast')}</strong>
                <small>{t('booking.participants')}</small>
              </div>
              <NumericCounter
                label={t('booking.breakfast')}
                value={state.breakfastParticipants}
                max={guestTotal}
                onChange={(value) =>
                  dispatch({
                    type: 'mealParticipantsChanged',
                    field: 'breakfastParticipants',
                    value,
                  })
                }
              />
            </div>
            <div className="booking-meal-option">
              <Moon aria-hidden="true" />
              <div>
                <strong>{t('booking.dinner')}</strong>
                <small>{t('booking.participants')}</small>
              </div>
              <NumericCounter
                label={t('booking.dinner')}
                value={state.dinnerParticipants}
                max={guestTotal}
                onChange={(value) =>
                  dispatch({
                    type: 'mealParticipantsChanged',
                    field: 'dinnerParticipants',
                    value,
                  })
                }
              />
            </div>
          </div>
        </section>

        <section className="booking-quote-bar" aria-live="polite">
          <div className="booking-quote-facts">
            <span>
              <BedDouble aria-hidden="true" />
              <strong>{selectedRoomCount}</strong> {t('booking.roomsShort')}
            </span>
            <span>
              <Users aria-hidden="true" />
              <strong>{guestTotal}</strong> {t('booking.guestsShort')}
            </span>
            <span>
              <Moon aria-hidden="true" />
              <strong>{nights}</strong> {t('booking.nightsShort')}
            </span>
          </div>

          <div className="booking-quote-price">
            {quoteStatus === 'loading' && <p>{t('booking.quoteLoading')}</p>}
            {quoteStatus === 'idle' && !largeGroup && <p>{t('booking.completeSelection')}</p>}
            {quoteStatus === 'error' && (
              <div className="booking-quote-error" role="alert">
                <span>{t('booking.quoteError')}</span>
                <button type="button" onClick={() => setQuoteRetry((value) => value + 1)}>
                  <RefreshCw aria-hidden="true" size={16} />
                  {t('booking.retry')}
                </button>
              </div>
            )}
            {quote && quoteStatus !== 'error' && (
              <>
                <dl>
                  <div>
                    <dt>{t('booking.accommodation')}</dt>
                    <dd>{formatMoney(quote.priceBreakdown.accommodationTotal, quote.currency)}</dd>
                  </div>
                  {quote.priceBreakdown.breakfastTotal > 0 && (
                    <div>
                      <dt>{t('booking.breakfast')}</dt>
                      <dd>{formatMoney(quote.priceBreakdown.breakfastTotal, quote.currency)}</dd>
                    </div>
                  )}
                  {quote.priceBreakdown.dinnerTotal > 0 && (
                    <div>
                      <dt>{t('booking.dinner')}</dt>
                      <dd>{formatMoney(quote.priceBreakdown.dinnerTotal, quote.currency)}</dd>
                    </div>
                  )}
                </dl>
                <p className="booking-grand-total">
                  <span>{t('booking.total')}</span>
                  <strong>{formatMoney(quote.priceBreakdown.totalPayable, quote.currency)}</strong>
                </p>
              </>
            )}
            <div className="booking-quote-notes" aria-label={t('booking.priceInformation')}>
              {touristTaxPercentage !== undefined && (
                <span>{t('booking.touristTaxNotice', { percentage: touristTaxPercentage })}</span>
              )}
              <span>{t('booking.currencyNotice')}</span>
            </div>
          </div>

          <div className="booking-quote-action">
            {largeGroup ? (
              <div className="booking-large-group">
                <Phone aria-hidden="true" />
                <p>
                  <strong>{t('booking.largeGroupTitle')}</strong>
                  <span>
                    {t('booking.largeGroupMessage', { threshold: LARGE_GROUP_THRESHOLD })}
                  </span>
                </p>
                <a href="tel:+40743677812">+40 743 677 812</a>
              </div>
            ) : (
              <>
                <button type="submit" disabled={!canContinue}>
                  {t('booking.continueToContact')}
                </button>
                <p>
                  <LockKeyhole aria-hidden="true" size={16} />
                  {t('booking.requestNotice')}
                </p>
              </>
            )}
          </div>
        </section>
      </form>
    </main>
  )
}
