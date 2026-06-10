import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'

const contentAreas = ['admin.translations', 'admin.rooms', 'admin.tourism'] as const

export function AdminContentPage() {
  const { t } = useTranslation()

  return (
    <>
      <SectionHeader title={t('admin.content')} lead={t('admin.lead')} />
      <div className="admin-grid">
        {contentAreas.map((area) => (
          <article className="admin-card" key={area}>
            <h2>{t(area)}</h2>
            <p>{t('admin.owner')}</p>
          </article>
        ))}
      </div>
    </>
  )
}
