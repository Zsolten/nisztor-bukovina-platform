import { ArrowRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useOutletContext } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'

const DESTINATIONS = [
  {
    id: 'heritage',
    image: '/images/destinations/deva-citadel.jpg',
    titleKey: 'homepage.surroundings.items.heritage.title',
    descriptionKey: 'homepage.surroundings.items.heritage.description',
    altKey: 'homepage.surroundings.items.heritage.imageAlt',
  },
  {
    id: 'saxonLand',
    image: '/images/destinations/sibiu-old-town.jpg',
    titleKey: 'homepage.surroundings.items.saxonLand.title',
    descriptionKey: 'homepage.surroundings.items.saxonLand.description',
    altKey: 'homepage.surroundings.items.saxonLand.imageAlt',
  },
  {
    id: 'mining',
    image: '/images/destinations/turda-salt-mine.jpg',
    titleKey: 'homepage.surroundings.items.mining.title',
    descriptionKey: 'homepage.surroundings.items.mining.description',
    altKey: 'homepage.surroundings.items.mining.imageAlt',
  },
  {
    id: 'nature',
    image: '/images/destinations/retezat-mountains.jpg',
    titleKey: 'homepage.surroundings.items.nature.title',
    descriptionKey: 'homepage.surroundings.items.nature.description',
    altKey: 'homepage.surroundings.items.nature.imageAlt',
  },
] as const

export default function HomepageSurroundings() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const attractionsPath = `/${language}/star-tours?view=attractions`

  return (
    <section className="homepage-band surroundings-section" aria-labelledby="surroundings-heading">
      <div className="homepage-inner">
        <header className="homepage-heading">
          <div>
            <p className="eyebrow">{t('homepage.surroundings.eyebrow')}</p>
            <h2 id="surroundings-heading">{t('homepage.surroundings.title')}</h2>
            <p className="surroundings-introduction">{t('homepage.surroundings.introduction')}</p>
            <Link className="surroundings-cta" to={attractionsPath}>
              {t('homepage.surroundings.exploreAttractions')}
              <ArrowRight aria-hidden="true" size={17} strokeWidth={1.8} />
            </Link>
          </div>
        </header>

        <div className="surroundings-grid">
          {DESTINATIONS.map((destination) => (
            <article className="destination-item" key={destination.id}>
              <Link
                className="destination-image-wrap"
                to={attractionsPath}
                aria-label={`${t(destination.titleKey)} – ${t('homepage.surroundings.exploreAttractions')}`}
              >
                <img src={destination.image} alt={t(destination.altKey)} loading="lazy" />
              </Link>
              <div className="destination-copy">
                <h3>{t(destination.titleKey)}</h3>
                <p>{t(destination.descriptionKey)}</p>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  )
}
