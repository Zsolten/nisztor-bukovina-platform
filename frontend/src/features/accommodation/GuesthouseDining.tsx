import { useTranslation } from 'react-i18next'

const DINING_IMAGES = [
  '/images/guesthouses/food/food-1.jpg',
  '/images/guesthouses/food/food-2.jpg',
] as const

export default function GuesthouseDining() {
  const { t } = useTranslation()

  return (
    <section
      className="detail-sheet editorial-section dining-section"
      aria-labelledby="dining-heading"
    >
      <div className="editorial-copy">
        <p className="section-index">02</p>
        <p className="eyebrow">{t('guesthouses.diningEyebrow')}</p>
        <h2 id="dining-heading">{t('guesthouses.diningTitle')}</h2>
        <p>{t('guesthouses.diningDescription')}</p>
      </div>
      <div
        className="editorial-images editorial-images--food"
        aria-label={t('guesthouses.diningImages')}
      >
        <figure>
          <img src={DINING_IMAGES[0]} alt={t('guesthouses.foodGrillAlt')} />
        </figure>
        <figure>
          <img src={DINING_IMAGES[1]} alt={t('guesthouses.foodTableAlt')} />
        </figure>
      </div>
    </section>
  )
}
