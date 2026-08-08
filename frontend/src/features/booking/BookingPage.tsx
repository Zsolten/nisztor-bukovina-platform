import { useEffect, useLayoutEffect, useReducer, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate, useOutletContext, useParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import type { GuesthouseSummary } from '../../shared/api/guesthouses'
import AsyncStatus from '../../shared/components/AsyncStatus'
import { useGuesthouse, useGuesthouses } from '../accommodation/useGuesthouseData'
import { bookingReducer, initialBookingFlowState } from './bookingReducer'

const DEFAULT_VISIBLE_SERVICE_COUNT = 6

function wrappedRowCount(itemWidths: number[], containerWidth: number, gap: number) {
  if (itemWidths.length === 0) return 0

  let rows = 1
  let occupiedWidth = 0

  itemWidths.forEach((itemWidth) => {
    const width = Math.min(itemWidth, containerWidth)
    const nextWidth = occupiedWidth === 0 ? width : occupiedWidth + gap + width

    if (occupiedWidth > 0 && nextWidth > containerWidth) {
      rows += 1
      occupiedWidth = width
    } else {
      occupiedWidth = nextWidth
    }
  })

  return rows
}

export default function BookingPage() {
  const { slug: routeSlug } = useParams()
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [state, dispatch] = useReducer(bookingReducer, initialBookingFlowState)
  const [servicesExpandedFor, setServicesExpandedFor] = useState<string | null>(null)
  const [collapsedServiceCount, setCollapsedServiceCount] = useState(DEFAULT_VISIBLE_SERVICE_COUNT)
  const servicesSectionRef = useRef<HTMLElement>(null)
  const servicesMeasureRef = useRef<HTMLUListElement>(null)
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
  const servicesExpanded = state.guesthouseId !== null && servicesExpandedFor === state.guesthouseId
  const visibleServices = servicesExpanded
    ? selection?.amenities
    : selection?.amenities.slice(0, collapsedServiceCount)

  useLayoutEffect(() => {
    const section = servicesSectionRef.current
    const measureList = servicesMeasureRef.current
    if (!section || !measureList || !selection) return
    const serviceSection = section
    const measurementList = measureList

    function updateVisibleServiceCount() {
      const heading = serviceSection.querySelector('h3')
      const serviceItems = Array.from(
        measurementList.querySelectorAll<HTMLElement>('[data-measure-service]'),
      )
      const moreItem = measurementList.querySelector<HTMLElement>('[data-measure-more]')
      if (!heading || !moreItem || serviceItems.length === 0 || measurementList.clientWidth === 0)
        return

      const listStyle = window.getComputedStyle(measurementList)
      const headingStyle = window.getComputedStyle(heading)
      const gap = Number.parseFloat(listStyle.columnGap || listStyle.gap) || 0
      const rowHeight = Math.max(
        moreItem.getBoundingClientRect().height,
        ...serviceItems.map((item) => item.getBoundingClientRect().height),
      )
      if (rowHeight === 0) return

      const headingMarginBottom = Number.parseFloat(headingStyle.marginBottom) || 0
      const availableHeight =
        serviceSection.clientHeight - heading.clientHeight - headingMarginBottom
      const compactRows = Math.max(1, Math.floor((availableHeight + gap) / (rowHeight + gap)))
      const maxRows = window.matchMedia('(max-width: 900px)').matches ? 2 : compactRows
      const serviceWidths = serviceItems.map((item) => item.getBoundingClientRect().width)
      const moreWidth = moreItem.getBoundingClientRect().width

      if (wrappedRowCount(serviceWidths, measurementList.clientWidth, gap) <= maxRows) {
        setCollapsedServiceCount(serviceItems.length)
        return
      }

      for (let count = serviceItems.length - 1; count >= 0; count -= 1) {
        const widthsWithMoreButton = [...serviceWidths.slice(0, count), moreWidth]
        if (wrappedRowCount(widthsWithMoreButton, measurementList.clientWidth, gap) <= maxRows) {
          setCollapsedServiceCount(count)
          return
        }
      }
    }

    updateVisibleServiceCount()
    if (typeof ResizeObserver === 'undefined') return

    const observer = new ResizeObserver(updateVisibleServiceCount)
    observer.observe(serviceSection)
    observer.observe(measurementList)
    return () => observer.disconnect()
  }, [selection, t])

  return (
    <main id="main-content" className="booking-page">
      <header className="booking-intro">
        <h1>{t('booking.title')}</h1>
        <p>{t('booking.introduction')}</p>
      </header>

      <form className="booking-entry-form" onSubmit={(event) => event.preventDefault()}>
        <fieldset className="guesthouse-choice">
          <legend className="visually-hidden">{t('booking.selectLegend')}</legend>

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
                  </label>
                )
              })}
            </div>
          )}
        </fieldset>

        <fieldset className="booking-dependent-step" disabled={!state.guesthouseId}>
          <legend className="visually-hidden">{t('booking.selectionDetails')}</legend>

          {!state.guesthouseId && <p className="booking-locked-note">{t('booking.locked')}</p>}
          {state.guesthouseId && selectedGuesthouse.loading && (
            <AsyncStatus variant="loading" message={t('booking.loadingSelection')} />
          )}
          {state.guesthouseId && selectedGuesthouse.error && (
            <AsyncStatus variant="error" message={t('booking.selectionError')} />
          )}

          {selection && (
            <div className="booking-selection-preview">
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

                <section ref={servicesSectionRef} aria-labelledby="booking-services">
                  <h3 id="booking-services">{t('booking.services')}</h3>
                  <ul className="booking-service-list">
                    {visibleServices?.map((amenity) => (
                      <li key={amenity.id}>{amenity.name}</li>
                    ))}
                    {selection.amenities.length > collapsedServiceCount && (
                      <li className="booking-more-item">
                        <button
                          type="button"
                          aria-expanded={servicesExpanded}
                          onClick={() =>
                            setServicesExpandedFor(servicesExpanded ? null : state.guesthouseId)
                          }
                        >
                          {servicesExpanded
                            ? t('booking.showLess')
                            : t('booking.moreServices', {
                                count: selection.amenities.length - collapsedServiceCount,
                              })}
                        </button>
                      </li>
                    )}
                  </ul>
                  <ul
                    ref={servicesMeasureRef}
                    className="booking-service-list booking-service-measure"
                    aria-hidden="true"
                  >
                    {selection.amenities.map((amenity) => (
                      <li key={amenity.id} data-label={amenity.name} data-measure-service />
                    ))}
                    <li className="booking-more-item" data-measure-more>
                      <button
                        type="button"
                        tabIndex={-1}
                        data-label={t('booking.moreServices', {
                          count: selection.amenities.length,
                        })}
                      />
                    </li>
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
