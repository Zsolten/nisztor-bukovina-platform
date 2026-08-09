import { CheckCircle2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link, useOutletContext, useSearchParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'

export default function BookingRequestSuccessPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const [searchParams] = useSearchParams()
  const reference = searchParams.get('reference')

  return (
    <main id="main-content" className="booking-page booking-success-page">
      <section className="booking-success-card" aria-labelledby="booking-success-heading">
        <CheckCircle2 aria-hidden="true" size={42} />
        <p>{t('booking.requestReceivedEyebrow')}</p>
        <h1 id="booking-success-heading">{t('booking.requestReceivedTitle')}</h1>
        <p>{t('booking.requestReceivedMessage')}</p>
        {reference && (
          <p className="booking-reference">
            <span>{t('booking.reference')}</span>
            <strong>{reference}</strong>
          </p>
        )}
        <p>{t('booking.paymentOnSite')}</p>
        <Link to={`/${language}`}>{t('booking.backToHome')}</Link>
      </section>
    </main>
  )
}
