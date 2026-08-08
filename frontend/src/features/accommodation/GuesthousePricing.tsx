import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import { useTranslation } from 'react-i18next'
import type { GuesthousePricing as Pricing } from '../../shared/api/guesthouses'
import BookingPlaceholderButton from './BookingPlaceholderButton'

interface GuesthousePricingProps {
  pricing: Pricing
}

const UNIT_LABELS = {
  person_night: 'guesthouses.priceUnits.person_night',
  person: 'guesthouses.priceUnits.person',
  day: 'guesthouses.priceUnits.day',
} as const

export default function GuesthousePricing({ pricing }: GuesthousePricingProps) {
  const { t } = useTranslation()

  if (pricing.items.length === 0) return null

  return (
    <section
      className="detail-sheet detail-sheet--right pricing-sheet"
      aria-labelledby="pricing-heading"
    >
      <header className="detail-sheet-heading pricing-heading">
        <p className="section-index">05</p>
        <h2 id="pricing-heading">{t('guesthouses.pricing')}</h2>
        <BookingPlaceholderButton />
      </header>
      <Row className="pricing-grid">
        <Col xs={12} lg={8}>
          <div className="price-list">
            {pricing.items.map((item) => (
              <div className="price-row" key={item.id}>
                <div>
                  <strong>{item.label}</strong>
                  <span>{t(UNIT_LABELS[item.unit])}</span>
                </div>
                <p>{`${item.amount} ${pricing.currency}`}</p>
              </div>
            ))}
          </div>
        </Col>
        <Col xs={12} lg={4}>
          <div className="pricing-notes">
            {pricing.taxes.map((tax) => (
              <div key={tax.id}>
                <span>{tax.label}</span>
                <strong>{tax.percentage}%</strong>
              </div>
            ))}
            {pricing.surcharges.map((adjustment) => (
              <div key={adjustment.id}>
                <span>{adjustment.label}</span>
                <strong>{adjustment.percentage}%</strong>
              </div>
            ))}
            {pricing.discounts.length > 0 && <h3>{t('guesthouses.discounts')}</h3>}
            {pricing.discounts.map((adjustment) => (
              <div key={adjustment.id}>
                <span>{adjustment.label}</span>
                <strong>{adjustment.percentage}%</strong>
              </div>
            ))}
            <p>{pricing.paymentNote}</p>
          </div>
        </Col>
      </Row>
    </section>
  )
}
