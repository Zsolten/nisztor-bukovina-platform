import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import { useTranslation } from 'react-i18next'
import type { GuesthouseDetail } from '../../shared/api/guesthouses'

interface GuesthouseQuickFactsProps {
  guesthouse: GuesthouseDetail
}

export default function GuesthouseQuickFacts({ guesthouse }: GuesthouseQuickFactsProps) {
  const { t } = useTranslation()
  const hasPrivateBathroom = guesthouse.roomTypes.some((room) =>
    room.features.includes('private-bathroom'),
  )

  return (
    <section
      className="detail-sheet detail-sheet--right quick-facts-sheet"
      aria-labelledby="quick-facts-heading"
    >
      <h2 id="quick-facts-heading">{t('guesthouses.quickFacts')}</h2>
      <Row className="quick-facts-grid">
        <Col xs={12} sm={4}>
          <p className="fact-label">{t('guesthouses.rooms')}</p>
          <strong>{t('guesthouses.roomCount', { count: guesthouse.roomCount })}</strong>
        </Col>
        <Col xs={12} sm={4}>
          <p className="fact-label">{t('guesthouses.location')}</p>
          <strong>{guesthouse.address.formatted}</strong>
        </Col>
        {hasPrivateBathroom && (
          <Col xs={12} sm={4}>
            <p className="fact-label">{t('guesthouses.privateBathroom')}</p>
            <strong aria-hidden="true">✓</strong>
            <span className="visually-hidden">{t('guesthouses.privateBathroom')}</span>
          </Col>
        )}
      </Row>
    </section>
  )
}
