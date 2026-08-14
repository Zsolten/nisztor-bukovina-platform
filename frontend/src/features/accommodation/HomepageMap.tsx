import { useTranslation } from 'react-i18next'

const MAP_URL = 'https://www.google.com/maps?q=45.8234696,22.9360273&z=17&output=embed'
const EXTERNAL_MAP_URL = 'https://www.google.com/maps/place/Pensiunea+Bukovina/@45.8232847,22.9322946,17z/data=!4m9!3m8!1s0x474e8c95c0541131:0xc8782fdf2f1f66e1!5m2!4m1!1i2!8m2!3d45.823281!4d22.9348695!16s%2Fg%2F11ddwzk7fn?entry=ttu&g_ep=EgoyMDI2MDgxMS4wIKXMDSoASAFQAw%3D%3D](https://www.google.com/maps/place/Pensiunea+Bukovina/@45.8232847,22.9322946,17z/data=!4m9!3m8!1s0x474e8c95c0541131:0xc8782fdf2f1f66e1!5m2!4m1!1i2!8m2!3d45.823281!4d22.9348695!16s%2Fg%2F11ddwzk7fn?entry=ttu&g_ep=EgoyMDI2MDgxMS4wIKXMDSoASAFQAw%3D%3D)'

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
