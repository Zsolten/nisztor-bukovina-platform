import { useTranslation } from 'react-i18next'

const MAP_URL =
  'https://www.openstreetmap.org/export/embed.html?bbox=22.84%2C45.80%2C22.95%2C45.90&layer=mapnik&marker=45.846%2C22.897'
const EXTERNAL_MAP_URL =
  'https://www.openstreetmap.org/?mlat=45.846&mlon=22.897#map=14/45.846/22.897'

export default function HomepageMap() {
  const { t } = useTranslation()

  return (
    <section className="homepage-band map-section" aria-labelledby="map-heading">
      <div className="homepage-inner map-layout">
        <div className="map-copy">
          <p className="section-index">05</p>
          <p className="eyebrow">{t('homepage.map.eyebrow')}</p>
          <h2 id="map-heading">{t('homepage.map.title')}</h2>
          <p>{t('homepage.map.description')}</p>
          <a className="text-link" href={EXTERNAL_MAP_URL} target="_blank" rel="noreferrer">
            {t('homepage.map.openMap')}
            <span aria-hidden="true">↗</span>
          </a>
        </div>
        <div className="map-frame-wrap">
          <iframe
            src={MAP_URL}
            title={t('homepage.map.frameTitle')}
            loading="lazy"
            referrerPolicy="no-referrer"
          />
        </div>
      </div>
    </section>
  )
}
