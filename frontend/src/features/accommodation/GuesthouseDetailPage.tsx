import Container from 'react-bootstrap/Container'
import { useTranslation } from 'react-i18next'
import { Link, useOutletContext, useParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import AsyncStatus from '../../shared/components/AsyncStatus'
import GuesthouseAmenities from './GuesthouseAmenities'
import GuesthouseContact from './GuesthouseContact'
import GuesthouseGallery from './GuesthouseGallery'
import GuesthousePricing from './GuesthousePricing'
import GuesthouseQuickFacts from './GuesthouseQuickFacts'
import GuesthouseRoomTypes from './GuesthouseRoomTypes'
import GuesthouseStory from './GuesthouseStory'
import { useGuesthouse } from './useGuesthouseData'

export default function GuesthouseDetailPage() {
  const { slug = '' } = useParams()
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const { data, loading, error } = useGuesthouse(slug, language)

  if (loading) {
    return (
      <Container as="main" fluid id="main-content" className="detail-status px-0">
        <AsyncStatus variant="loading" message={t('guesthouses.loading')} />
      </Container>
    )
  }

  if (error || !data) {
    return (
      <Container as="main" fluid id="main-content" className="detail-status px-0">
        <AsyncStatus variant="error" message={t('guesthouses.detailError')} />
        <Link className="text-link" to={`/${language}`}>
          ← {t('guesthouses.back')}
        </Link>
      </Container>
    )
  }

  return (
    <Container as="main" fluid id="main-content" className="guesthouse-detail px-0">
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

      <GuesthouseQuickFacts guesthouse={data} />

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

      <div className="detail-sheet-stack">
        <GuesthouseStory
          description={data.description}
          roomDescription={data.roomDescription}
          history={data.history}
        />
        <GuesthouseRoomTypes roomTypes={data.roomTypes} />
        <GuesthouseAmenities amenities={data.amenities} />
        <GuesthousePricing pricing={data.pricing} />
        <GuesthouseContact contacts={data.contacts} address={data.address} />
      </div>
    </Container>
  )
}
