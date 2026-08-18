import { ExternalLink, MapPin } from 'lucide-react'
import { Modal } from 'react-bootstrap'
import ModalManager from '@restart/ui/ModalManager'
import { useTranslation } from 'react-i18next'
import type { CSSProperties } from 'react'
import type {
  PublicAttraction,
  PublicStarTour,
  PublicStarTourRoute,
} from '../../shared/api/tourism'
import AttractionCategoryIcon from './AttractionCategoryIcon'
import { buildGoogleMapsDirectionsUrl } from './googleMapsDirections'
import { categoryForAttraction } from './tourismCategories'

type TourismDetail =
  { type: 'tour'; value: PublicStarTour } | { type: 'attraction'; value: PublicAttraction } | null

interface TourismDetailsModalProps {
  detail: TourismDetail
  route: PublicStarTourRoute | null
  immersiveMap: boolean
  onHide: () => void
}

const immersiveMapModalManager = new ModalManager({ handleContainerOverflow: false })

function formatDistance(meters: number | null) {
  return meters === null ? '—' : `${Math.round(meters / 1000)} km`
}

function formatDuration(seconds: number | null) {
  if (seconds === null) return '—'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.round((seconds % 3600) / 60)
  return hours > 0 ? `${hours} óra ${minutes} perc` : `${minutes} perc`
}

function descriptionParagraphs(description: string) {
  const explicitParagraphs = description
    .split(/\r?\n+/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean)

  if (explicitParagraphs.length > 1) return explicitParagraphs

  const sentences = (explicitParagraphs[0] ?? description)
    .match(/[^.!?]+[.!?]+|[^.!?]+$/g)
    ?.map((sentence) => sentence.trim())
    .filter(Boolean)

  if (!sentences || sentences.length <= 3) return explicitParagraphs

  return Array.from({ length: Math.ceil(sentences.length / 3) }, (_, index) =>
    sentences.slice(index * 3, index * 3 + 3).join(' '),
  )
}

export default function TourismDetailsModal({
  detail,
  route,
  immersiveMap,
  onHide,
}: TourismDetailsModalProps) {
  const { t } = useTranslation()
  const tour = detail?.type === 'tour' ? detail.value : null
  const attraction = detail?.type === 'attraction' ? detail.value : null
  const directionsUrl =
    tour && route?.routeStatus === 'READY'
      ? buildGoogleMapsDirectionsUrl(route.base, tour.stops)
      : null
  const headerMapUrl = attraction?.googleMapsUrl ?? directionsUrl

  return (
    <Modal
      centered
      className="tourism-details-modal"
      manager={immersiveMap ? immersiveMapModalManager : undefined}
      onHide={onHide}
      restoreFocus={false}
      scrollable
      show={detail !== null}
      size="lg"
    >
      <Modal.Header closeButton closeLabel={t('tourism.closeDetails')}>
        <div className="tourism-modal-header-content">
          <Modal.Title>{tour?.name ?? attraction?.name}</Modal.Title>
          {headerMapUrl && (
            <div className="tourism-modal-header-actions">
              {attraction && (
                <p className="tourism-attraction-category">
                  <AttractionCategoryIcon category={categoryForAttraction(attraction)} size={19} />
                  {t(`tourism.categories.${categoryForAttraction(attraction)}`)}
                </p>
              )}
              <a
                className="tourism-modal-map-link"
                aria-label={tour ? t('tourism.openFullRoute') : undefined}
                href={headerMapUrl}
                target="_blank"
                rel="noreferrer"
              >
                {t('tourism.googleMaps')}
                <ExternalLink aria-hidden="true" size={15} />
              </a>
            </div>
          )}
        </div>
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
            <blockquote className="tourism-modal-quote">„{tour.shortDescription}”</blockquote>
            <dl className="tourism-modal-facts">
              <div>
                <dt>{t('tourism.distance')}</dt>
                <dd>{formatDistance(tour.totals.travelDistanceMeters)}</dd>
              </div>
              <div>
                <dt>{t('tourism.travelDuration')}</dt>
                <dd>{formatDuration(tour.totals.travelDurationSeconds)}</dd>
              </div>
              <div>
                <dt>{t('tourism.totalVisitDuration')}</dt>
                <dd>{formatDuration(tour.totals.totalDurationSeconds)}</dd>
              </div>
              <div>
                <dt>{t('tourism.stopCount')}</dt>
                <dd>{tour.stops.length}</dd>
              </div>
            </dl>
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
            <section className="tourism-modal-detailed-description">
              <h2>{t('tourism.detailedDescription')}</h2>
              {descriptionParagraphs(tour.detailedDescription).map((paragraph, index) => (
                <p key={`${tour.slug}-${index}`}>{paragraph}</p>
              ))}
            </section>
          </article>
        )}
        {attraction && (
          <article className="tourism-modal-content tourism-modal-attraction">
            <blockquote className="tourism-modal-quote">{attraction.shortDescription}</blockquote>
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
            <section className="tourism-modal-detailed-description">
              <h2>{t('tourism.detailedDescription')}</h2>
              {descriptionParagraphs(attraction.detailedDescription).map((paragraph, index) => (
                <p key={`${attraction.slug}-${index}`}>{paragraph}</p>
              ))}
            </section>
          </article>
        )}
      </Modal.Body>
    </Modal>
  )
}
