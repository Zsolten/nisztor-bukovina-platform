import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'
import { dayTripCards } from '../../data/demoCatalog'

export function DayTripsPage() {
  const { t } = useTranslation()

  return (
    <section className="page">
      <SectionHeader title={t('dayTrips.title')} lead={t('dayTrips.lead')} />
      <div className="two-column-grid">
        {dayTripCards.map((trip) => (
          <article className="entity-card" key={trip.slug}>
            <span className="pill">{t('dayTrips.hours', { count: trip.hours })}</span>
            <h2>{t(trip.titleKey)}</h2>
            <p>{t(trip.descriptionKey)}</p>
          </article>
        ))}
      </div>
    </section>
  )
}
