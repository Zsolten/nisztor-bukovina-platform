import { useTranslation } from 'react-i18next'

export default function HomepageLegacy() {
  const { t } = useTranslation()

  return (
    <section className="homepage-band legacy-section" aria-labelledby="legacy-heading">
      <div className="homepage-inner legacy-layout">
        <div className="legacy-image-wrap">
          <img
            src="/images/homepage/family-hosts.jpg"
            alt={t('homepage.legacy.imageAlt')}
            loading="lazy"
          />
        </div>
        <div className="legacy-copy">
          <p className="eyebrow">{t('homepage.legacy.eyebrow')}</p>
          <h2 id="legacy-heading">{t('homepage.legacy.title')}</h2>
          <p>{t('homepage.legacy.body')}</p>
        </div>
      </div>
    </section>
  )
}
