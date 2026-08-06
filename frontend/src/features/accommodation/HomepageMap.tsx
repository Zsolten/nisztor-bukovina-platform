import { useTranslation } from 'react-i18next'

const MAP_URL = 'https://www.google.com/maps?q=45.82361,22.93869&z=15&output=embed'
const EXTERNAL_MAP_URL = 'https://www.google.com/maps/dir/?api=1&destination=45.82361,22.93869'

export default function HomepageMap() {
  const { t } = useTranslation()

  return (
    <section className="map-section" id="contact" aria-labelledby="map-heading">
      <div className="homepage-inner map-copy">
        <p className="eyebrow">{t('homepage.map.eyebrow')}</p>
        <h2 id="map-heading">{t('homepage.map.title')}</h2>
        <p>{t('homepage.map.description')}</p>
        <a className="text-link" href={EXTERNAL_MAP_URL} target="_blank" rel="noreferrer">
          {t('homepage.map.openMap')}
          <span aria-hidden="true">↗</span>
        </a>
      </div>
      <iframe
        className="homepage-map-frame"
        src={MAP_URL}
        title={t('homepage.map.frameTitle')}
        loading="lazy"
        referrerPolicy="no-referrer-when-downgrade"
      />
    </section>
  )
}
