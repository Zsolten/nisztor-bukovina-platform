import { useTranslation } from 'react-i18next'
import type { AmenityCategory, GuesthouseAmenity } from '../../shared/api/guesthouses'

interface GuesthouseAmenitiesProps {
  amenities: GuesthouseAmenity[]
  title: string
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

export default function GuesthouseAmenities({ amenities, title }: GuesthouseAmenitiesProps) {
  const { t } = useTranslation()

  if (amenities.length === 0) return null

  return (
    <section
      className="detail-sheet detail-sheet--left amenities-sheet"
      aria-labelledby="amenities-heading"
    >
      <header className="detail-sheet-heading">
        <p className="section-index">03</p>
        <h2 id="amenities-heading">{title}</h2>
      </header>
      <div className="amenity-groups">
        {CATEGORY_ORDER.map((category) => {
          const categoryAmenities = amenities.filter((amenity) => amenity.category === category)
          if (categoryAmenities.length === 0) return null

          return (
            <section className="amenity-group" key={category}>
              <h3>{t(CATEGORY_LABELS[category])}</h3>
              <ul>
                {categoryAmenities.map((amenity) => (
                  <li key={amenity.id}>
                    <div className="amenity-name-row">
                      <strong>{amenity.name}</strong>
                      {amenity.pricingType === 'PAID' && (
                        <span className="amenity-paid-tag">{t('guesthouses.paidService')}</span>
                      )}
                    </div>
                    {amenity.description && (
                      <span className="amenity-description">{amenity.description}</span>
                    )}
                  </li>
                ))}
              </ul>
            </section>
          )
        })}
      </div>
    </section>
  )
}
