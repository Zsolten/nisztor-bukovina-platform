import Badge from 'react-bootstrap/Badge'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import { useTranslation } from 'react-i18next'
import type { GuesthouseRoomType } from '../../shared/api/guesthouses'

interface GuesthouseRoomTypesProps {
  roomTypes: GuesthouseRoomType[]
  description: string
  title: string
}

export default function GuesthouseRoomTypes({
  roomTypes,
  description,
  title,
}: GuesthouseRoomTypesProps) {
  const { t } = useTranslation()

  if (roomTypes.length === 0) return null

  return (
    <section
      className="detail-sheet detail-sheet--right room-types-sheet"
      aria-labelledby="room-types-heading"
    >
      <header className="detail-sheet-heading">
        <p className="section-index">04</p>
        <div>
          <h2 id="room-types-heading">{title}</h2>
          <p className="section-introduction">{description}</p>
        </div>
      </header>
      <Row className="room-type-grid">
        {roomTypes.map((roomType) => (
          <Col xs={12} md={6} xl={4} key={roomType.id}>
            <Card as="article" className="room-type-card h-100">
              <Card.Body>
                <Card.Title as="h3">{roomType.name}</Card.Title>
                {roomType.shortDescription && <Card.Text>{roomType.shortDescription}</Card.Text>}
                <div className="room-type-facts">
                  <span>{t('guesthouses.capacity', { count: roomType.standardOccupancy })}</span>
                </div>
                {roomType.roomsWithExtraBed > 0 && (
                  <Badge as="span" className="information-tag" bg="light" text="dark">
                    {t('guesthouses.extraBed')}
                  </Badge>
                )}
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>
    </section>
  )
}
