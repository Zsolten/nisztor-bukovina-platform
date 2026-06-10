import { useTranslation } from 'react-i18next'
import { Link, useParams } from 'react-router-dom'
import { SectionHeader } from '../../components/SectionHeader'
import { guesthouses, rooms } from '../../data/demoCatalog'
import { localizedPath } from '../../routing/localizedPath'
import { useActiveLanguage } from '../../routing/useActiveLanguage'
import { NotFoundPage } from './NotFoundPage'

export function PropertyDetailPage() {
  const { t } = useTranslation()
  const language = useActiveLanguage()
  const { propertySlug } = useParams()
  const guesthouse = guesthouses.find((item) => item.slug === propertySlug)

  if (!guesthouse) {
    return <NotFoundPage />
  }

  const propertyRooms = rooms.filter((room) => room.propertySlug === guesthouse.slug)

  return (
    <section className="page">
      <SectionHeader
        eyebrow={t(guesthouse.locationKey)}
        title={guesthouse.name}
        lead={t(guesthouse.descriptionKey)}
      />
      <div className="content-grid">
        {propertyRooms.map((room) => (
          <article className="entity-card" key={room.slug}>
            <h2>{t(room.nameKey)}</h2>
            <p>{t(room.descriptionKey)}</p>
            <div className="metadata">
              <span className="pill">{t('rooms.capacity', { count: room.capacity })}</span>
              <span className="pill">{t('rooms.privateBathroom')}</span>
            </div>
          </article>
        ))}
      </div>
      <div className="summary-band">
        <div>
          <strong>{t('nav.booking')}</strong>
          <Link className="text-action" to={localizedPath(language, 'foglalasi-keres')}>
            {t('home.bookingCta')}
          </Link>
        </div>
        <div>
          <strong>{t('nav.attractions')}</strong>
          <Link className="text-action" to={localizedPath(language, 'latnivalok')}>
            {t('common.view')}
          </Link>
        </div>
        <div>
          <strong>{t('nav.dayTrips')}</strong>
          <Link className="text-action" to={localizedPath(language, 'csillagturak')}>
            {t('common.view')}
          </Link>
        </div>
      </div>
    </section>
  )
}
