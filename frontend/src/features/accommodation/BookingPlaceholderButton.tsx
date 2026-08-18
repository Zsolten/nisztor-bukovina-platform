import { useTranslation } from 'react-i18next'
import { Link, useOutletContext } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'

export default function BookingPlaceholderButton() {
  const { t } = useTranslation()
  const { language } = useOutletContext<LanguageOutletContext>()

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
