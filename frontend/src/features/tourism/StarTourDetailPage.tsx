import { ArrowLeft, ExternalLink, MapPin } from 'lucide-react'
import { useEffect, useState, type CSSProperties } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useOutletContext, useParams } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import { getPublicStarTour, type PublicStarTour } from '../../shared/api/tourism'

function formatDistance(meters: number | null) {
  return meters === null ? '—' : `${Math.round(meters / 1000)} km`
}

function formatDuration(seconds: number | null) {
  if (seconds === null) return '—'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.round((seconds % 3600) / 60)
  return hours > 0 ? `${hours} óra ${minutes} perc` : `${minutes} perc`
}

export default function StarTourDetailPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { slug } = useParams()
  const { t } = useTranslation()
  const [tour, setTour] = useState<PublicStarTour | null>(null)
  const [loading, setLoading] = useState(true)
  const [missing, setMissing] = useState(false)

  useEffect(() => {
    if (!slug) return
    const controller = new AbortController()

    void getPublicStarTour(slug, language, controller.signal)
      .then((response) => {
        setTour(response)
        setMissing(false)
      })
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) setMissing(true)
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })

    return () => controller.abort()
  }, [language, slug])

  if (loading) {
    return <main id="main-content" className="tourism-detail-state">{t('tourism.loading')}</main>
  }

  if (missing || !tour) {
    return (
      <main id="main-content" className="tourism-detail-state">
        <h1>{t('tourism.tourNotFoundTitle')}</h1>
        <Link to={`/${language}/star-tours`}>{t('tourism.backToTours')}</Link>
      </main>
    )
  }

  return (
    <main id="main-content" className="tourism-detail-page">
      <div className="tourism-detail-shell">
        <Link className="tourism-detail-back" to={`/${language}/star-tours`}>
          <ArrowLeft aria-hidden="true" size={18} />
          {t('tourism.backToTours')}
        </Link>

        <header className="tourism-detail-hero">
          <p className="eyebrow">{t('tourism.tours')}</p>
          <h1>{tour.name}</h1>
          <p>{tour.shortDescription}</p>
          <dl className="tourism-detail-facts">
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
        </header>

        {tour.images.length > 0 && (
          <section className="tourism-detail-gallery" aria-label={t('tourism.tourImages')}>
            {tour.images.map((image) => (
              <img key={image.imageUrl} src={image.imageUrl} alt={image.altText} />
            ))}
          </section>
        )}

        <div className="tourism-detail-content">
          <section>
            <h2>{t('tourism.aboutTour')}</h2>
            <p>{tour.detailedDescription}</p>
          </section>
          <section className="tourism-detail-stops">
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
        </div>
      </div>
    </main>
  )
}
