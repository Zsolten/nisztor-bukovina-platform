import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'

export default function BookingPlaceholderButton() {
  const { t } = useTranslation()

  return (
    <Link className="booking-placeholder-button" to="booking">
      {t('guesthouses.booking')}
    </Link>
  )
}
