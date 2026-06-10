import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'
import { rooms } from '../../data/demoCatalog'

export function RoomsPage() {
  const { t } = useTranslation()

  return (
    <section className="page">
      <SectionHeader title={t('rooms.title')} lead={t('rooms.lead')} />
      <div className="content-grid">
        {rooms.map((room) => (
          <article className="entity-card" key={room.slug}>
            <span className="pill">{room.propertyName}</span>
            <h2>{t(room.nameKey)}</h2>
            <p>{t(room.descriptionKey)}</p>
            <div className="metadata">
              <span className="pill">{t('rooms.capacity', { count: room.capacity })}</span>
              <span className="pill">{t('rooms.privateBathroom')}</span>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}
