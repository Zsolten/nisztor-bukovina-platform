import { useTranslation } from 'react-i18next'

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
  const { t } = useTranslation()

  return (
    <section className="homepage-band surroundings-section" aria-labelledby="surroundings-heading">
      <div className="homepage-inner">
        <header className="homepage-heading">
          <p className="section-index">03</p>
          <div>
            <p className="eyebrow">{t('homepage.surroundings.eyebrow')}</p>
            <h2 id="surroundings-heading">{t('homepage.surroundings.title')}</h2>
            <p>{t('homepage.surroundings.introduction')}</p>
          </div>
        </header>

        <div className="surroundings-grid">
          {DESTINATIONS.map((destination, index) => (
            <article className="destination-item" key={destination.id}>
              <div className="destination-image-wrap">
                <img src={destination.image} alt={t(destination.altKey)} loading="lazy" />
                <span aria-hidden="true">{String(index + 1).padStart(2, '0')}</span>
              </div>
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
