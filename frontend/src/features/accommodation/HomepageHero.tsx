import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

const HERO_IMAGES = [
  '/images/guesthouses/bukovina/gallery-01.jpg',
  '/images/guesthouses/amenities/amenity-01.jpg',
  '/images/guesthouses/nisztor/gallery-01.jpg',
  '/images/destinations/deva-citadel.jpg',
]

export default function HomepageHero() {
  const { t } = useTranslation()
  const [activeImageIndex, setActiveImageIndex] = useState(0)

  useEffect(() => {
    const intervalId = window.setInterval(() => {
      setActiveImageIndex((currentIndex) => (currentIndex + 1) % HERO_IMAGES.length)
    }, 6_000)

    return () => window.clearInterval(intervalId)
  }, [])

  return (
    <section className="guesthouse-hero" aria-label={t('homepage.hero.title')}>
      <div
        className="hero-slides"
        aria-hidden="true"
        style={{ backgroundImage: `url("${HERO_IMAGES[0]}")` }}
      >
        {HERO_IMAGES.map((image, index) => (
          <img
            className={`hero-slide${index === activeImageIndex ? ' hero-slide-active' : ''}`}
            src={image}
            alt=""
            loading="eager"
            decoding="async"
            fetchPriority={index === 0 ? 'high' : 'auto'}
            key={image}
          />
        ))}
      </div>
      <div className="hero-content">
        <div className="hero-logos">
          <img src="/images/logo/nisztor-logo.png" alt={t('homepage.hero.nisztorLogoAlt')} />
          <span className="hero-logo-divider" aria-hidden="true" />
          <img src="/images/logo/bukovina-logo.png" alt={t('homepage.hero.bukovinaLogoAlt')} />
        </div>
      </div>
    </section>
  )
}
