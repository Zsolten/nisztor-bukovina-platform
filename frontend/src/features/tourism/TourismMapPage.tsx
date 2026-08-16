import {
  ChevronRight,
  ExternalLink,
  Grid2X2,
  Heart,
  MapPinned,
  Search,
  SlidersHorizontal,
} from 'lucide-react'
import { useEffect, useMemo, useState, type CSSProperties } from 'react'
import { useTranslation } from 'react-i18next'
import { useOutletContext } from 'react-router-dom'
import type { LanguageOutletContext } from '../../app/LanguageLayout'
import {
  listPublicCachedStarTourRoutes,
  listPublicAttractions,
  listPublicStarTours,
  type PublicAttraction,
  type PublicStarTour,
  type PublicStarTourRoute,
} from '../../shared/api/tourism'
import AttractionCategoryIcon from './AttractionCategoryIcon'
import TourismDetailsModal from './TourismDetailsModal'
import TourismMap from './TourismMap'
import { categoryForAttraction, type AttractionCategory } from './tourismCategories'

type TourismView = 'tours' | 'attractions'
type DetailTarget =
  | { type: 'tour'; value: PublicStarTour }
  | { type: 'attraction'; value: PublicAttraction }
  | null

const FAVORITES_STORAGE_KEY = 'favoriteStarTours'
const TOUR_IMAGE_BY_SLUG: Record<string, string> = {
  'paring-es-hatszegi-medence': '/images/destinations/retezat-mountains.jpg',
  'maros-mente-es-gyulafehervar': '/images/destinations/deva-citadel.jpg',
}
const ATTRACTION_IMAGE_BY_SLUG: Record<string, string> = {
  'deva-vara': '/images/destinations/deva-citadel.jpg',
  gyulafehervar: '/images/destinations/alba-iulia-citadel.jpg',
  'paring-hegyseg': '/images/destinations/retezat-mountains.jpg',
  'veka-szurdok': '/images/destinations/red-ravine.jpg',
}
const ATTRACTION_CATEGORIES: AttractionCategory[] = ['castle', 'nature', 'church', 'museum']

function readFavorites() {
  try {
    const value = JSON.parse(window.localStorage.getItem(FAVORITES_STORAGE_KEY) ?? '[]')
    if (!Array.isArray(value)) return []
    return value
      .filter(
        (item): item is { tourId: string; addedAt: string } =>
          typeof item?.tourId === 'string' && typeof item?.addedAt === 'string',
      )
      .map((item) => item.tourId)
  } catch {
    return []
  }
}

function formatDistance(meters: number | null) {
  if (meters === null) return '—'
  return `${Math.round(meters / 1000)} km`
}

function formatDuration(seconds: number | null) {
  if (seconds === null) return '—'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.round((seconds % 3600) / 60)
  return hours > 0 ? `${hours} óra ${minutes} perc` : `${minutes} perc`
}

function tourImage(tour: PublicStarTour) {
  return tour.images[0]?.imageUrl || TOUR_IMAGE_BY_SLUG[tour.slug]
}

export default function TourismMapPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const [view, setView] = useState<TourismView>('tours')
  const [query, setQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<AttractionCategory | 'all'>('all')
  const [tours, setTours] = useState<PublicStarTour[]>([])
  const [attractions, setAttractions] = useState<PublicAttraction[]>([])
  const [selectedTourSlug, setSelectedTourSlug] = useState<string | null>(null)
  const [selectedAttraction, setSelectedAttraction] = useState<PublicAttraction | null>(null)
  const [routes, setRoutes] = useState<PublicStarTourRoute[]>([])
  const [favorites, setFavorites] = useState<string[]>(readFavorites)
  const [detailTarget, setDetailTarget] = useState<DetailTarget>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)

  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY?.trim() ?? ''
  const mapId = import.meta.env.VITE_GOOGLE_MAPS_MAP_ID?.trim()

  useEffect(() => {
    const controller = new AbortController()
    void Promise.all([
      listPublicStarTours(language, controller.signal),
      listPublicAttractions(language, controller.signal),
      listPublicCachedStarTourRoutes(controller.signal),
    ])
      .then(([tourResponse, attractionResponse, routeResponse]) => {
        setLoadError(false)
        setTours(tourResponse)
        setAttractions(attractionResponse)
        setRoutes(routeResponse)
        setSelectedTourSlug((current) =>
          current && tourResponse.some((tour) => tour.slug === current)
            ? current
            : (tourResponse[0]?.slug ?? null),
        )
      })
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) setLoadError(true)
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })

    return () => controller.abort()
  }, [language])

  const selectedTour = tours.find((tour) => tour.slug === selectedTourSlug) ?? null
  const selectedRoute = routes.find((route) => route.tourSlug === selectedTourSlug) ?? null
  const normalizedQuery = query.trim().toLocaleLowerCase(language)
  const filteredTours = useMemo(
    () =>
      tours.filter((tour) =>
        `${tour.name} ${tour.shortDescription} ${tour.stops.map((stop) => stop.name).join(' ')}`
          .toLocaleLowerCase(language)
          .includes(normalizedQuery),
      ),
    [language, normalizedQuery, tours],
  )
  const filteredAttractions = useMemo(
    () =>
      attractions.filter(
        (attraction) =>
          `${attraction.name} ${attraction.shortDescription}`
            .toLocaleLowerCase(language)
            .includes(normalizedQuery) &&
          (selectedCategory === 'all' || categoryForAttraction(attraction) === selectedCategory),
      ),
    [attractions, language, normalizedQuery, selectedCategory],
  )

  function toggleFavorite(slug: string) {
    setFavorites((current) => {
      const next = current.includes(slug)
        ? current.filter((favorite) => favorite !== slug)
        : [...current, slug]
      window.localStorage.setItem(
        FAVORITES_STORAGE_KEY,
        JSON.stringify(next.map((tourId) => ({ tourId, addedAt: new Date().toISOString() }))),
      )
      return next
    })
  }

  const mapAttractions = view === 'tours' ? attractions : filteredAttractions
  const mapRoutes = useMemo(
    () =>
      routes
        .map((route) => {
          const tour = tours.find((item) => item.slug === route.tourSlug)
          return tour ? { route, color: tour.mapColor } : null
        })
        .filter((route): route is { route: PublicStarTourRoute; color: string } => route !== null),
    [routes, tours],
  )

  return (
    <main id="main-content" className="tourism-page">
      <section className="tourism-sidebar" aria-labelledby="tourism-title">
        <div className="tourism-intro">
          <p className="eyebrow">{t('tourism.eyebrow')}</p>
          <h1 id="tourism-title">{t('tourism.title')}</h1>
          <p>{t('tourism.introduction')}</p>
        </div>

        <div className="tourism-view-switch" role="tablist" aria-label={t('tourism.viewLabel')}>
          <button
            type="button"
            role="tab"
            aria-selected={view === 'tours'}
            className={view === 'tours' ? 'active' : ''}
            onClick={() => setView('tours')}
          >
            {t('tourism.tours')}
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={view === 'attractions'}
            className={view === 'attractions' ? 'active' : ''}
            onClick={() => setView('attractions')}
          >
            {t('tourism.attractions')}
          </button>
        </div>

        <label className="tourism-search">
          <Search aria-hidden="true" size={23} />
          <span className="visually-hidden">{t('tourism.searchLabel')}</span>
          <input
            type="search"
            value={query}
            placeholder={
              view === 'tours' ? t('tourism.searchTours') : t('tourism.searchAttractions')
            }
            onChange={(event) => setQuery(event.target.value)}
          />
          {view === 'attractions' && (
            <span className="tourism-search-filter" aria-hidden="true">
              <SlidersHorizontal size={19} />
            </span>
          )}
        </label>

        {view === 'attractions' && (
          <div className="tourism-category-filters" aria-label={t('tourism.categoryFilter')}>
            <button
              type="button"
              className={selectedCategory === 'all' ? 'active' : ''}
              aria-pressed={selectedCategory === 'all'}
              onClick={() => setSelectedCategory('all')}
            >
              <Grid2X2 aria-hidden="true" size={20} />
              {t('tourism.categories.all')}
            </button>
            {ATTRACTION_CATEGORIES.map((category) => (
              <button
                key={category}
                type="button"
                className={selectedCategory === category ? 'active' : ''}
                aria-pressed={selectedCategory === category}
                onClick={() => setSelectedCategory(category)}
              >
                <AttractionCategoryIcon category={category} size={20} />
                {t(`tourism.categories.${category}`)}
              </button>
            ))}
          </div>
        )}

        {loading && <p className="tourism-state">{t('tourism.loading')}</p>}
        {loadError && <p className="tourism-state tourism-state-error">{t('tourism.error')}</p>}

        {!loading && !loadError && view === 'tours' && (
          <div className="tourism-tour-list" aria-live="polite">
            {filteredTours.map((tour) => {
              const selected = tour.slug === selectedTourSlug
              const image = tourImage(tour)
              const previewStops =
                tour.stops.length > 3
                  ? [tour.stops[0], null, tour.stops.at(-1)!]
                  : tour.stops
              return (
                <article
                  key={tour.slug}
                  className={`tourism-tour-card${selected ? ' selected' : ''}`}
                  style={{ '--tour-color': tour.mapColor } as CSSProperties}
                >
                  <button
                    className="tourism-tour-select"
                    type="button"
                    aria-label={`${tour.name} ${t('tourism.selectTour')}`}
                    onClick={() => setSelectedTourSlug(tour.slug)}
                  >
                    {selected && <span aria-hidden="true">✓</span>}
                  </button>
                  {image && <img src={image} alt={tour.images[0]?.altText || ''} />}
                  <div className="tourism-tour-copy">
                    <span className="tourism-route-swatch" aria-hidden="true" />
                    <h2>{tour.name}</h2>
                    <p className="tourism-tour-meta">
                      {formatDistance(tour.totals.travelDistanceMeters)} ·{' '}
                      {formatDuration(tour.totals.totalDurationSeconds)}
                    </p>
                    <ol className="tourism-tour-stops">
                      {previewStops.map((stop, index) =>
                        stop ? (
                          <li key={stop.slug}>
                            <span>{index === previewStops.length - 1 ? tour.stops.length : 1}</span>
                            {stop.name}
                          </li>
                        ) : (
                          <li key="more" className="tourism-tour-stops-more">
                            <span aria-hidden="true">⋮</span>
                            {t('tourism.intermediateStops', { count: tour.stops.length - 2 })}
                          </li>
                        ),
                      )}
                    </ol>
                  </div>
                  <button
                    className={`tourism-favorite${favorites.includes(tour.slug) ? ' active' : ''}`}
                    type="button"
                    aria-label={t('tourism.favorite', { name: tour.name })}
                    aria-pressed={favorites.includes(tour.slug)}
                    onClick={() => toggleFavorite(tour.slug)}
                  >
                    <Heart aria-hidden="true" fill="currentColor" size={25} />
                  </button>
                  <button
                    type="button"
                    className="tourism-tour-cta"
                    onClick={() => setDetailTarget({ type: 'tour', value: tour })}
                  >
                    {t('tourism.showTour')}
                  </button>
                </article>
              )
            })}
            {filteredTours.length === 0 && <p className="tourism-state">{t('tourism.noTours')}</p>}
          </div>
        )}

        {!loading && !loadError && view === 'attractions' && (
          <section className="tourism-attraction-section" aria-labelledby="attractions-heading">
            <div className="tourism-list-heading">
              <h2 id="attractions-heading">{t('tourism.attractions')}</h2>
              <p>{t('tourism.placeCount', { count: filteredAttractions.length })}</p>
            </div>
            <div className="tourism-attraction-list">
              {filteredAttractions.map((attraction) => {
                const image = ATTRACTION_IMAGE_BY_SLUG[attraction.slug]
                return (
                  <article
                    key={attraction.slug}
                    className={`tourism-attraction-card${image ? ' has-image' : ''}`}
                  >
                    {image && <img src={image} alt="" />}
                    <div className="tourism-attraction-copy">
                      <p className="tourism-attraction-category">
                        <AttractionCategoryIcon
                          category={categoryForAttraction(attraction)}
                          size={19}
                        />
                        {t(`tourism.categories.${categoryForAttraction(attraction)}`)}
                      </p>
                      <h3>{attraction.name}</h3>
                      <p>{attraction.shortDescription}</p>
                      <div className="tourism-attraction-actions">
                        <button
                          type="button"
                          onClick={() => setDetailTarget({ type: 'attraction', value: attraction })}
                        >
                          {t('tourism.details')}
                          <ChevronRight aria-hidden="true" size={18} />
                        </button>
                        <a href={attraction.googleMapsUrl} target="_blank" rel="noreferrer">
                          {t('tourism.googleMaps')}
                          <ExternalLink aria-hidden="true" size={17} />
                        </a>
                      </div>
                    </div>
                  </article>
                )
              })}
              {filteredAttractions.length === 0 && (
                <p className="tourism-state">{t('tourism.noAttractions')}</p>
              )}
            </div>
          </section>
        )}
      </section>

      <section className={`tourism-map-panel${view === 'attractions' ? ' attractions-mode' : ''}`}>
        {!apiKey ? (
          <div className="tourism-map-configuration" role="status">
            <MapPinned aria-hidden="true" size={38} />
            <h2>{t('tourism.mapConfigurationTitle')}</h2>
            <p>{t('tourism.mapConfigurationText')}</p>
          </div>
        ) : (
          <TourismMap
            apiKey={apiKey}
            mapId={mapId}
            language={language}
            attractions={mapAttractions}
            selectedAttraction={selectedAttraction}
            selectedRoute={view === 'tours' ? selectedRoute : null}
            routes={view === 'tours' ? mapRoutes : []}
            onSelectAttraction={setSelectedAttraction}
            onOpenAttractionDetails={(attraction) =>
              setDetailTarget({ type: 'attraction', value: attraction })
            }
          />
        )}
        <div className="tourism-map-caption">
          <strong>{t('tourism.mapCaptionTitle')}</strong>
          <span>{t('tourism.mapCaptionText')}</span>
        </div>
        {view === 'tours' &&
          selectedTour &&
          !selectedRoute && (
            <p className="tourism-route-status">
              {t(`tourism.routeStatus.${selectedTour.routeStatus}`)}
            </p>
          )}
      </section>
      <TourismDetailsModal detail={detailTarget} onHide={() => setDetailTarget(null)} />
    </main>
  )
}
