import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'
import { attractionCards } from '../../data/demoCatalog'

export function AttractionsPage() {
  const { t } = useTranslation()

  return (
    <section className="page">
      <SectionHeader title={t('tourism.title')} lead={t('tourism.lead')} />
      <div className="content-grid">
        {attractionCards.map((card) => (
          <article className="entity-card" key={card.slug}>
            <span className="pill">{t(card.categoryKey)}</span>
            <h2>{t(card.titleKey)}</h2>
            <p>{t(card.descriptionKey)}</p>
          </article>
        ))}
      </div>
    </section>
  )
}
