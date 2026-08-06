import { useTranslation } from 'react-i18next'
import type { GuesthouseImage } from '../../shared/api/guesthouses'
import BookingPlaceholderButton from './BookingPlaceholderButton'

interface GuesthouseStoryProps {
  description: string
  images: GuesthouseImage[]
}

export default function GuesthouseStory({ description, images }: GuesthouseStoryProps) {
  const { t } = useTranslation()

  return (
    <section className="detail-sheet editorial-section story-sheet" aria-labelledby="story-heading">
      <div className="editorial-images" aria-label={t('guesthouses.storyImages')}>
        {images.map((image) => (
          <figure key={image.path}>
            <img src={image.path} alt={image.altText} />
          </figure>
        ))}
      </div>
      <div className="editorial-copy">
        <p className="section-index">01</p>
        <p className="eyebrow">{t('guesthouses.welcome')}</p>
        <h2 id="story-heading">{t('guesthouses.villageTitle')}</h2>
        <p>{description}</p>
        <BookingPlaceholderButton />
      </div>
    </section>
  )
}
