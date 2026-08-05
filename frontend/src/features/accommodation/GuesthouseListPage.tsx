import { useTranslation } from 'react-i18next'
import { Link, useOutletContext } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import AsyncStatus from '../../shared/components/AsyncStatus'
import { useGuesthouses } from './useGuesthouseData'

export default function GuesthouseListPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const { data, loading, error } = useGuesthouses(language)

  return (
    <main id="main-content">
      <section className="guesthouse-hero" aria-labelledby="guesthouse-heading">
        <div className="hero-ornament" aria-hidden="true">
          <span />
          <span />
          <span />
          <span />
        </div>
        <div className="hero-copy">
          <p className="eyebrow">{t('guesthouses.eyebrow')}</p>
          <h1 id="guesthouse-heading">{t('guesthouses.title')}</h1>
          <p className="hero-introduction">{t('guesthouses.introduction')}</p>
        </div>
        <div className="hero-place" aria-hidden="true">
          <span>Csernakeresztúr</span>
          <span>Dél-Erdély</span>
        </div>
      </section>

      <section className="guesthouse-section" aria-labelledby="guesthouse-list-heading">
        <header className="section-heading">
          <p className="section-index">01</p>
          <div>
            <h2 id="guesthouse-list-heading">{t('guesthouses.sectionTitle')}</h2>
            <p>{t('guesthouses.sectionIntroduction')}</p>
          </div>
        </header>

        {loading && <AsyncStatus variant="loading" message={t('guesthouses.loading')} />}
        {error && <AsyncStatus variant="error" message={t('guesthouses.loadError')} />}

        {data && (
          <div className="guesthouse-grid">
            {data.map((guesthouse, index) => (
              <article className="guesthouse-card" key={guesthouse.slug}>
                <Link
                  className="guesthouse-image-link"
                  to={`/${language}/guesthouses/${guesthouse.slug}`}
                  aria-label={`${guesthouse.name} – ${t('guesthouses.openDetails')}`}
                >
                  <img
                    src={guesthouse.coverImage.path}
                    alt={guesthouse.coverImage.altText}
                    loading={index === 0 ? 'eager' : 'lazy'}
                  />
                  <span className="card-number" aria-hidden="true">
                    0{index + 1}
                  </span>
                </Link>
                <div className="guesthouse-card-copy">
                  <p className="room-count">
                    {t('guesthouses.roomCount', { count: guesthouse.roomCount })}
                  </p>
                  <h3>{guesthouse.name}</h3>
                  <p>{guesthouse.shortDescription}</p>
                  <Link className="text-link" to={`/${language}/guesthouses/${guesthouse.slug}`}>
                    {t('guesthouses.openDetails')}
                    <span aria-hidden="true">↗</span>
                  </Link>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}
