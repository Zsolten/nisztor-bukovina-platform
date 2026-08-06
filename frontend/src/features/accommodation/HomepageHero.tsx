import { useTranslation } from 'react-i18next'

const HERO_IMAGES = [
  '/images/guesthouses/nisztor/gallery-01.jpg',
  '/images/guesthouses/bukovina/gallery-01.jpg',
  '/images/homepage/family-hosts.jpg',
  '/images/destinations/retezat-mountains.jpg',
]

export default function HomepageHero() {
  const { t } = useTranslation()

  return (
    <section className="guesthouse-hero" aria-labelledby="guesthouse-heading">
      <div className="hero-slides" aria-hidden="true">
        {HERO_IMAGES.map((image) => (
          <img className="hero-slide" src={image} alt="" key={image} />
        ))}
      </div>
      <div className="hero-content">
        <div className="hero-logos">
          <img src="/images/logo/nisztor-logo.png" alt={t('homepage.hero.nisztorLogoAlt')} />
          <span className="hero-logo-divider" aria-hidden="true" />
          <img src="/images/logo/bukovina-logo.png" alt={t('homepage.hero.bukovinaLogoAlt')} />
        </div>
        <h1 id="guesthouse-heading">{t('homepage.hero.title')}</h1>
      </div>
    </section>
  )
}
