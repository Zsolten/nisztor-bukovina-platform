import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { localizedPath } from '../../routing/localizedPath'
import { useActiveLanguage } from '../../routing/useActiveLanguage'

export function HomePage() {
  const { t } = useTranslation()
  const language = useActiveLanguage()

  return (
    <section className="page">
      <div className="hero-section">
        <div className="hero-copy">
          <span className="eyebrow">{t('home.eyebrow')}</span>
          <h1 className="hero-title">{t('home.title')}</h1>
          <p className="lead">{t('home.lead')}</p>
          <div className="hero-actions">
            <Link className="primary-action" to={localizedPath(language, 'foglalasi-keres')}>
              {t('home.bookingCta')}
            </Link>
            <Link className="secondary-action" to={localizedPath(language, 'programajanlo')}>
              {t('home.itineraryCta')}
            </Link>
          </div>
        </div>

        <aside className="hero-facts" aria-label="Platform summary">
          <div className="fact-row">
            <strong>{t('home.facts.noGuestLogin')}</strong>
            <span>{t('home.facts.noGuestLoginText')}</span>
          </div>
          <div className="fact-row">
            <strong>{t('home.facts.localContent')}</strong>
            <span>{t('home.facts.localContentText')}</span>
          </div>
          <div className="fact-row">
            <strong>{t('home.facts.modular')}</strong>
            <span>{t('home.facts.modularText')}</span>
          </div>
        </aside>
      </div>

      <div className="feature-grid">
        <article className="entity-card">
          <h2>{t('home.featureBooking')}</h2>
          <p>{t('booking.lead')}</p>
        </article>
        <article className="entity-card">
          <h2>{t('home.featureTourism')}</h2>
          <p>{t('tourism.lead')}</p>
        </article>
        <article className="entity-card">
          <h2>{t('home.featureAi')}</h2>
          <p>{t('itinerary.lead')}</p>
        </article>
      </div>
    </section>
  )
}
