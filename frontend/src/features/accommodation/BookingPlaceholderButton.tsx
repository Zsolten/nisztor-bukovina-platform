import { useTranslation } from 'react-i18next'
import { Link } from 'react-router-dom'
import type { Language } from '../../i18n/languages'

interface BookingPlaceholderButtonProps {
  language: Language
}

export default function BookingPlaceholderButton({ language }: BookingPlaceholderButtonProps) {
  const { t } = useTranslation()

  return (
    <Link
      className="booking-placeholder-button"
      to={`/${language}/booking`}
      onClick={() => window.scrollTo(0, 0)}
    >
      {t('guesthouses.booking')}
    </Link>
  )
}
