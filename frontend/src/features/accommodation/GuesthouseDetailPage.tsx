import { useTranslation } from 'react-i18next'
import { Link, useOutletContext, useParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import GuesthouseGallery from './GuesthouseGallery'
import { useGuesthouse } from './useGuesthouseData'

export default function GuesthouseDetailPage() {
  const { slug = '' } = useParams()
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const { data, loading, error } = useGuesthouse(slug, language)

  if (loading) {
    return (
      <main id="main-content" className="detail-status">
        <p className="status-message">{t('guesthouses.loading')}</p>
      </main>
    )
  }

  if (error || !data) {
    return (
      <main id="main-content" className="detail-status">
        <p className="status-message error">{t('guesthouses.detailError')}</p>
        <Link className="text-link" to={`/${language}`}>
          ← {t('guesthouses.back')}
        </Link>
      </main>
    )
  }

  return (
    <main id="main-content" className="guesthouse-detail">
      <div className="detail-back-row">
        <Link className="back-link" to={`/${language}`}>
          <span aria-hidden="true">←</span> {t('guesthouses.back')}
        </Link>
        <span>{t('guesthouses.roomCount', { count: data.roomCount })}</span>
      </div>

      <section className="detail-hero" aria-labelledby="detail-heading">
        <img src={data.coverImage.path} alt={data.coverImage.altText} />
        <div className="detail-title-card">
          <p className="eyebrow">{t('app.location')}</p>
          <h1 id="detail-heading">{data.name}</h1>
          <p>{data.shortDescription}</p>
        </div>
      </section>

      <section className="story-section">
        <p className="section-index">02</p>
        <div>
          <p className="story-lead">{data.description}</p>
          <div className="room-note">
            <p className="eyebrow">{t('guesthouses.rooms')}</p>
            <p>{data.roomDescription}</p>
          </div>
        </div>
      </section>

      <section className="gallery-section" aria-labelledby="gallery-heading">
        <header className="section-heading compact">
          <p className="section-index">03</p>
          <div>
            <h2 id="gallery-heading">{t('guesthouses.gallery')}</h2>
            <p>{t('guesthouses.galleryHint')}</p>
          </div>
        </header>
        <GuesthouseGallery images={data.images} />
      </section>
    </main>
  )
}
