import { useTranslation } from 'react-i18next'

export default function BookingPlaceholderButton() {
  const { t } = useTranslation()

  return (
    <button className="booking-placeholder-button" type="button" aria-disabled="true">
      {t('guesthouses.booking')}
    </button>
  )
}
