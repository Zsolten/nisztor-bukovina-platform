import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import type { Language } from '../../i18n/languages'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'
import GuesthouseAmenities from './GuesthouseAmenities'
import GuesthouseDining from './GuesthouseDining'
import GuesthouseGallery from './GuesthouseGallery'
import GuesthouseHistory from './GuesthouseHistory'
import GuesthousePricing from './GuesthousePricing'
import GuesthouseRoomTypes from './GuesthouseRoomTypes'
import GuesthouseStory from './GuesthouseStory'

export type GuesthouseContentSection = 'hero' | 'story' | 'rooms' | 'history'

interface GuesthouseDetailContentProps {
  data: GuesthouseDetail
  language: Language
  showBackRow?: boolean
  wrapSection?: (section: GuesthouseContentSection, content: ReactNode) => ReactNode
}

export default function GuesthouseDetailContent({
  data,
  language,
  showBackRow = true,
  wrapSection = (_section, content) => content,
}: GuesthouseDetailContentProps) {
  const { t } = useTranslation()
  const storyImages = data.images.filter((image) => !image.cover).slice(0, 2)
  while (storyImages.length < 2) storyImages.push(data.coverImage)

  return (
    <>
      {showBackRow && (
        <div className="detail-back-row">
          <Link className="back-link" to={`/${language}`}>
            <span aria-hidden="true">←</span> {t('guesthouses.back')}
          </Link>
          <span>{t('guesthouses.roomCount', { count: data.roomCount })}</span>
        </div>
      )}

      {wrapSection(
        'hero',
        <section className="detail-hero" aria-labelledby="detail-heading">
          <img src={data.coverImage.path} alt={data.coverImage.altText} />
          <div className="detail-title-card">
            <p className="eyebrow">{t('app.location')}</p>
            <h1 id="detail-heading">{data.name}</h1>
            <p>{data.shortDescription}</p>
          </div>
        </section>,
      )}

      <div className="detail-sheet-stack">
        {wrapSection(
          'story',
          <GuesthouseStory description={data.description} images={storyImages} />,
        )}
        <GuesthouseDining />
        <GuesthouseAmenities amenities={data.amenities} />
        {wrapSection(
          'rooms',
          <GuesthouseRoomTypes roomTypes={data.roomTypes} description={data.roomDescription} />,
        )}
        <GuesthousePricing pricing={data.pricing} />
        {wrapSection('history', <GuesthouseHistory history={data.history} />)}
      </div>

      <section className="gallery-section" aria-labelledby="gallery-heading">
        <header className="section-heading compact">
          <p className="section-index">07</p>
          <div>
            <h2 id="gallery-heading">{t('guesthouses.gallery')}</h2>
            <p>{t('guesthouses.galleryHint')}</p>
          </div>
        </header>
        <GuesthouseGallery images={data.images} />
      </section>
    </>
  )
}
