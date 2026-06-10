import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import { SectionHeader } from '../../components/SectionHeader'
import { guesthouses } from '../../data/demoCatalog'
import { localizedPath } from '../../routing/localizedPath'
import { useActiveLanguage } from '../../routing/useActiveLanguage'

export function GuesthousesPage() {
  const { t } = useTranslation()
  const language = useActiveLanguage()

  return (
    <section className="page">
      <SectionHeader title={t('guesthouses.title')} lead={t('guesthouses.lead')} />
      <div className="two-column-grid">
        {guesthouses.map((guesthouse) => (
          <article className="entity-card" key={guesthouse.slug}>
            <div>
              <h2>{guesthouse.name}</h2>
              <p>{t(guesthouse.locationKey)}</p>
            </div>
            <p>{t(guesthouse.descriptionKey)}</p>
            <Link className="text-action" to={localizedPath(language, `panzioink/${guesthouse.slug}`)}>
              {t('guesthouses.details')}
            </Link>
          </article>
        ))}
      </div>
    </section>
  )
}
