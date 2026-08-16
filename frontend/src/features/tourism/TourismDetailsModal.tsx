import { ExternalLink, MapPin } from 'lucide-react'
import { Modal } from 'react-bootstrap'
import { useTranslation } from 'react-i18next'
import type { CSSProperties } from 'react'
import type { PublicAttraction, PublicStarTour } from '../../shared/api/tourism'
import AttractionCategoryIcon from './AttractionCategoryIcon'
import { categoryForAttraction } from './tourismCategories'

type TourismDetail =
  | { type: 'tour'; value: PublicStarTour }
  | { type: 'attraction'; value: PublicAttraction }
  | null

interface TourismDetailsModalProps {
  detail: TourismDetail
  onHide: () => void
}

function formatDistance(meters: number | null) {
  return meters === null ? '—' : `${Math.round(meters / 1000)} km`
}

function formatDuration(seconds: number | null) {
  if (seconds === null) return '—'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.round((seconds % 3600) / 60)
  return hours > 0 ? `${hours} óra ${minutes} perc` : `${minutes} perc`
}

export default function TourismDetailsModal({ detail, onHide }: TourismDetailsModalProps) {
  const { t } = useTranslation()
  const tour = detail?.type === 'tour' ? detail.value : null
  const attraction = detail?.type === 'attraction' ? detail.value : null

  return (
    <Modal
      centered
      className="tourism-details-modal"
      onHide={onHide}
      scrollable
      show={detail !== null}
      size="lg"
    >
      <Modal.Header closeButton closeLabel={t('tourism.closeDetails')}>
        <Modal.Title>{tour?.name ?? attraction?.name}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {tour && (
          <article className="tourism-modal-content tourism-modal-tour">
            {tour.images.length > 0 && (
              <div className="tourism-modal-gallery" aria-label={t('tourism.tourImages')}>
                {tour.images.map((image) => (
                  <img key={image.imageUrl} src={image.imageUrl} alt={image.altText} />
                ))}
              </div>
            )}
            <p className="tourism-modal-summary">{tour.shortDescription}</p>
            <dl className="tourism-modal-facts">
              <div>
                <dt>{t('tourism.distance')}</dt>
                <dd>{formatDistance(tour.totals.travelDistanceMeters)}</dd>
              </div>
              <div>
                <dt>{t('tourism.duration')}</dt>
                <dd>{formatDuration(tour.totals.totalDurationSeconds)}</dd>
              </div>
              <div>
                <dt>{t('tourism.stopCount')}</dt>
                <dd>{tour.stops.length}</dd>
              </div>
            </dl>
            <section>
              <h2>{t('tourism.aboutTour')}</h2>
              <p className="tourism-modal-description">{tour.detailedDescription}</p>
            </section>
            <section className="tourism-modal-stops">
              <h2>{t('tourism.tourStops')}</h2>
              <ol style={{ '--tour-color': tour.mapColor } as CSSProperties}>
                {tour.stops.map((stop, index) => (
                  <li key={stop.slug}>
                    <span>{index + 1}</span>
                    <div>
                      <strong>{stop.name}</strong>
                      {stop.optional && <em>{t('tourism.optionalStop')}</em>}
                      <a href={stop.googleMapsUrl} target="_blank" rel="noreferrer">
                        <MapPin aria-hidden="true" size={15} />
                        {t('tourism.openOnMap')}
                        <ExternalLink aria-hidden="true" size={13} />
                      </a>
                    </div>
                  </li>
                ))}
              </ol>
            </section>
          </article>
        )}
        {attraction && (
          <article className="tourism-modal-content tourism-modal-attraction">
            <p className="tourism-attraction-category">
              <AttractionCategoryIcon category={categoryForAttraction(attraction)} size={19} />
              {t(`tourism.categories.${categoryForAttraction(attraction)}`)}
            </p>
            <p className="tourism-modal-summary">{attraction.shortDescription}</p>
            <p className="tourism-modal-description">{attraction.detailedDescription}</p>
            {attraction.admissionInformation && (
              <section>
                <h2>{t('tourism.admissionInformation')}</h2>
                <p>{attraction.admissionInformation}</p>
              </section>
            )}
            {attraction.practicalInformation && (
              <section>
                <h2>{t('tourism.practicalInformation')}</h2>
                <p>{attraction.practicalInformation}</p>
              </section>
            )}
            <a
              className="tourism-modal-map-link"
              href={attraction.googleMapsUrl}
              target="_blank"
              rel="noreferrer"
            >
              {t('tourism.googleMaps')}
              <ExternalLink aria-hidden="true" size={17} />
            </a>
          </article>
        )}
      </Modal.Body>
    </Modal>
  )
}
