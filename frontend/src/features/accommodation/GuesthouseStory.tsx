import { useTranslation } from 'react-i18next'
import type { Language } from '../../i18n/languages'
import type { GuesthouseImage } from '../../shared/api/guesthouses'
import BookingPlaceholderButton from './BookingPlaceholderButton'

interface GuesthouseStoryProps {
  description: string
  images: GuesthouseImage[]
  eyebrow: string
  language: Language
  title: string
}

export default function GuesthouseStory({
  description,
  images,
  eyebrow,
  language,
  title,
}: GuesthouseStoryProps) {
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
        <p className="eyebrow">{eyebrow}</p>
        <h2 id="story-heading">{title}</h2>
        <p>{description}</p>
        <BookingPlaceholderButton language={language} />
      </div>
    </section>
  )
}
