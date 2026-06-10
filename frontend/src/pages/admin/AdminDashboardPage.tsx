import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'

export function AdminDashboardPage() {
  const { t } = useTranslation()

  return (
    <>
      <SectionHeader title={t('admin.dashboard')} lead={t('admin.lead')} />
      <div className="admin-grid">
        <article className="admin-card">
          <h2>{t('admin.bookings')}</h2>
          <p>{t('booking.lead')}</p>
        </article>
        <article className="admin-card">
          <h2>{t('admin.rooms')}</h2>
          <p>{t('rooms.lead')}</p>
        </article>
        <article className="admin-card">
          <h2>{t('admin.tourism')}</h2>
          <p>{t('tourism.lead')}</p>
        </article>
      </div>
    </>
  )
}
