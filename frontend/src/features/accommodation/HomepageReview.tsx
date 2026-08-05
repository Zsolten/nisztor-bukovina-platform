import { useTranslation } from 'react-i18next'

export default function HomepageReview() {
  const { t } = useTranslation()

  return (
    <section className="homepage-band review-section" aria-labelledby="review-heading">
      <div className="homepage-inner review-layout">
        <div className="review-heading-wrap">
          <p className="section-index">04</p>
          <p className="eyebrow">{t('homepage.review.eyebrow')}</p>
          <h2 id="review-heading">{t('homepage.review.title')}</h2>
        </div>
        <blockquote>
          <p>“{t('homepage.review.quote')}”</p>
          <footer>{t('homepage.review.attribution')}</footer>
        </blockquote>
      </div>
    </section>
  )
}
