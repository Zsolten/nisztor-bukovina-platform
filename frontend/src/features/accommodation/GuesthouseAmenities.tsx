import Badge from 'react-bootstrap/Badge'
import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import { useTranslation } from 'react-i18next'
import type { AmenityCategory, GuesthouseAmenity } from '../../shared/api/guesthouses'

interface GuesthouseAmenitiesProps {
  amenities: GuesthouseAmenity[]
}

const CATEGORY_ORDER: AmenityCategory[] = [
  'ROOM_COMFORT',
  'FOOD_KITCHEN',
  'OUTDOOR_WELLNESS',
  'PROGRAM_GROUP',
]

const CATEGORY_LABELS = {
  ROOM_COMFORT: 'guesthouses.amenityCategories.ROOM_COMFORT',
  FOOD_KITCHEN: 'guesthouses.amenityCategories.FOOD_KITCHEN',
  OUTDOOR_WELLNESS: 'guesthouses.amenityCategories.OUTDOOR_WELLNESS',
  PROGRAM_GROUP: 'guesthouses.amenityCategories.PROGRAM_GROUP',
} as const

export default function GuesthouseAmenities({ amenities }: GuesthouseAmenitiesProps) {
  const { t } = useTranslation()

  if (amenities.length === 0) return null

  return (
    <section
      className="detail-sheet detail-sheet--left amenities-sheet"
      aria-labelledby="amenities-heading"
    >
      <header className="detail-sheet-heading">
        <p className="section-index">05</p>
        <h2 id="amenities-heading">{t('guesthouses.amenities')}</h2>
      </header>
      <Row className="amenity-groups">
        {CATEGORY_ORDER.map((category) => {
          const categoryAmenities = amenities.filter((amenity) => amenity.category === category)
          if (categoryAmenities.length === 0) return null

          return (
            <Col xs={12} md={6} key={category}>
              <section className="amenity-group">
                <h3>{t(CATEGORY_LABELS[category])}</h3>
                <div className="amenity-tags">
                  {categoryAmenities.map((amenity) => (
                    <Badge
                      as="span"
                      className="amenity-tag"
                      bg="light"
                      text="dark"
                      key={amenity.id}
                      title={amenity.description}
                    >
                      {amenity.name}
                    </Badge>
                  ))}
                </div>
              </section>
            </Col>
          )
        })}
      </Row>
    </section>
  )
}
