import { useEffect, useReducer } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useOutletContext, useParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import type { GuesthouseSummary } from '../../shared/api/guesthouses'
import AsyncStatus from '../../shared/components/AsyncStatus'
import { useGuesthouse, useGuesthouses } from '../accommodation/useGuesthouseData'
import { bookingReducer, initialBookingFlowState } from './bookingReducer'

export default function BookingPage() {
  const { slug: routeSlug } = useParams()
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(bookingReducer, initialBookingFlowState)
  const guesthouses = useGuesthouses(language)
  const selectedGuesthouse = useGuesthouse(state.guesthouseSlug, language)

  useEffect(() => {
    if (!routeSlug || !guesthouses.data) return

    const routeGuesthouse = guesthouses.data.find((guesthouse) => guesthouse.slug === routeSlug)
    if (!routeGuesthouse) return

    dispatch({
      type: 'guesthouseSelected',
      guesthouseId: routeGuesthouse.id,
      guesthouseSlug: routeGuesthouse.slug,
    })
  }, [guesthouses.data, routeSlug])

  function selectGuesthouse(guesthouse: GuesthouseSummary) {
    dispatch({
      type: 'guesthouseSelected',
      guesthouseId: guesthouse.id,
      guesthouseSlug: guesthouse.slug,
    })
    void navigate(`/${language}/guesthouses/${guesthouse.slug}/booking`, { replace: true })
  }

  const selection = selectedGuesthouse.data
  const canContinue = Boolean(state.guesthouseId && selection && !selectedGuesthouse.error)

  return (
    <main id="main-content" className="booking-page">
      <header className="booking-intro">
        <p className="eyebrow">{t('booking.eyebrow')}</p>
        <h1>{t('booking.title')}</h1>
        <p>{t('booking.introduction')}</p>
      </header>

      <form className="booking-entry-form" onSubmit={(event) => event.preventDefault()}>
        <fieldset className="guesthouse-choice">
          <legend>{t('booking.selectLegend')}</legend>
          <p>{t('booking.selectHint')}</p>

          {guesthouses.loading && (
            <AsyncStatus variant="loading" message={t('booking.loadingGuesthouses')} />
          )}
          {guesthouses.error && (
            <AsyncStatus variant="error" message={t('booking.guesthousesError')} />
          )}

          {guesthouses.data && (
            <div className="guesthouse-choice-grid">
              {guesthouses.data.map((guesthouse) => {
                const selected = guesthouse.id === state.guesthouseId
                return (
                  <label
                    className={`guesthouse-choice-card${selected ? ' is-selected' : ''}`}
                    key={guesthouse.id}
                  >
                    <input
                      type="radio"
                      name="guesthouseId"
                      value={guesthouse.id}
                      checked={selected}
                      onChange={() => selectGuesthouse(guesthouse)}
                    />
                    <img src={guesthouse.coverImage.path} alt="" />
                    <span>
                      <strong>{guesthouse.name}</strong>
                      <small>{guesthouse.shortDescription}</small>
                    </span>
                    <span className="choice-indicator" aria-hidden="true">
                      {selected ? '✓' : ''}
                    </span>
                  </label>
                )
              })}
            </div>
          )}
        </fieldset>

        <fieldset className="booking-dependent-step" disabled={!state.guesthouseId}>
          <legend>{t('booking.selectionDetails')}</legend>

          {!state.guesthouseId && <p className="booking-locked-note">{t('booking.locked')}</p>}
          {state.guesthouseId && selectedGuesthouse.loading && (
            <AsyncStatus variant="loading" message={t('booking.loadingSelection')} />
          )}
          {state.guesthouseId && selectedGuesthouse.error && (
            <AsyncStatus variant="error" message={t('booking.selectionError')} />
          )}

          {selection && (
            <div className="booking-selection-preview">
              <div className="booking-selection-heading">
                <img src={selection.coverImage.path} alt={selection.coverImage.altText} />
                <div>
                  <p>{t('booking.selectedLabel')}</p>
                  <h2>{selection.name}</h2>
                </div>
              </div>

              <div className="booking-selection-columns">
                <section aria-labelledby="booking-room-types">
                  <h3 id="booking-room-types">{t('booking.roomTypes')}</h3>
                  <ul>
                    {selection.roomTypes.map((roomType) => (
                      <li key={roomType.id}>
                        <strong>{roomType.name}</strong>
                        <span>
                          {t('booking.roomCapacity', { count: roomType.standardOccupancy })}
                        </span>
                      </li>
                    ))}
                  </ul>
                </section>

                <section aria-labelledby="booking-prices">
                  <h3 id="booking-prices">{t('booking.prices')}</h3>
                  <ul>
                    {selection.pricing.items.map((item) => (
                      <li key={item.id}>
                        <strong>{item.label}</strong>
                        <span>
                          {item.amount} {selection.pricing.currency}
                        </span>
                      </li>
                    ))}
                  </ul>
                </section>

                <section aria-labelledby="booking-services">
                  <h3 id="booking-services">{t('booking.services')}</h3>
                  <ul>
                    {selection.amenities.map((amenity) => (
                      <li key={amenity.id}>{amenity.name}</li>
                    ))}
                  </ul>
                </section>
              </div>
            </div>
          )}
        </fieldset>

        <footer className="booking-entry-actions">
          <p>{t('booking.requestNotice')}</p>
          <button type="submit" disabled={!canContinue}>
            {t('booking.continue')}
          </button>
        </footer>
      </form>
    </main>
  )
}
