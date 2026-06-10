import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'

const bookingRows = [
  { guest: 'Minta vendég', property: 'Nisztor panzió', status: 'OWNER_REVIEW' },
  { guest: 'Demo guest', property: 'Bukovina panzió', status: 'RECEIVED' },
] as const

export function AdminBookingsPage() {
  const { t } = useTranslation()

  return (
    <>
      <SectionHeader title={t('admin.bookings')} lead={t('booking.lead')} />
      <div className="admin-panel">
        <table className="status-table">
          <thead>
            <tr>
              <th>{t('booking.name')}</th>
              <th>{t('booking.guesthouse')}</th>
              <th>{t('admin.status')}</th>
            </tr>
          </thead>
          <tbody>
            {bookingRows.map((row) => (
              <tr key={`${row.guest}-${row.property}`}>
                <td>{row.guest}</td>
                <td>{row.property}</td>
                <td>{row.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}
