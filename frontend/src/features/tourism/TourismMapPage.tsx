import {
  ChevronRight,
  ExternalLink,
  Grid2X2,
  Heart,
  List,
  Map as MapIcon,
  MapPinned,
  Search,
} from 'lucide-react'
import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type CSSProperties,
  type KeyboardEvent as ReactKeyboardEvent,
} from 'react'
import { useTranslation } from 'react-i18next'
import { useOutletContext } from 'react-router-dom'
import { useSwipeable } from 'react-swipeable'
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
type TourismLayout = 'map' | 'list'
type DetailTarget =
  { type: 'tour'; value: PublicStarTour } | { type: 'attraction'; value: PublicAttraction } | null

const MOBILE_ATTRACTION_FOCUS_ZOOM = 10
const DESKTOP_ATTRACTION_FOCUS_ZOOM = 9
const FAVORITES_STORAGE_KEY = 'favoriteStarTours'
const MOBILE_TOURISM_QUERY = '(max-width: 767.98px), (max-width: 900px) and (max-height: 600px)'
const TOUR_IMAGE_BY_SLUG: Record<string, string> = {
  'paring-es-hatszegi-medence': '/images/destinations/retezat-mountains.jpg',
  'maros-mente-es-gyulafehervar': '/images/destinations/deva-citadel.jpg',
}
const TOUR_FALLBACK_IMAGE = '/images/destinations/sibiu-old-town.jpg'
const ATTRACTION_IMAGE_BY_SLUG: Record<string, string> = {
  'deva-vara': '/images/destinations/deva-citadel.jpg',
  gyulafehervar: '/images/destinations/alba-iulia-citadel.jpg',
  'paring-hegyseg': '/images/destinations/retezat-mountains.jpg',
  'veka-szurdok': '/images/destinations/red-ravine.jpg',
}
const ATTRACTION_FALLBACK_IMAGE_BY_CATEGORY: Record<AttractionCategory, string> = {
  castle: '/images/destinations/deva-citadel.jpg',
  nature: '/images/destinations/retezat-mountains.jpg',
  church: '/images/destinations/alba-iulia-citadel.jpg',
  museum: '/images/destinations/turda-salt-mine.jpg',
  other: '/images/destinations/sibiu-old-town.jpg',
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
  return tour.images[0]?.imageUrl || TOUR_IMAGE_BY_SLUG[tour.slug] || TOUR_FALLBACK_IMAGE
}

function attractionImage(attraction: PublicAttraction) {
  return (
    ATTRACTION_IMAGE_BY_SLUG[attraction.slug] ||
    ATTRACTION_FALLBACK_IMAGE_BY_CATEGORY[categoryForAttraction(attraction)]
  )
}

function normalizeSearchText(value: string, language: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase(language)
}

export default function TourismMapPage() {
  const { language } = useOutletContext<LanguageOutletContext>()
  const { t } = useTranslation()
  const [view, setView] = useState<TourismView>('tours')
  const [layout, setLayout] = useState<TourismLayout>('map')
  const [query, setQuery] = useState('')
  const [mobileCardAnimation, setMobileCardAnimation] = useState<'next' | 'previous' | null>(null)
  const [compactViewport, setCompactViewport] = useState(
    () => window.matchMedia(MOBILE_TOURISM_QUERY).matches,
  )
  const [selectedCategory, setSelectedCategory] = useState<AttractionCategory | 'all'>('all')
  const [tours, setTours] = useState<PublicStarTour[]>([])
  const [attractions, setAttractions] = useState<PublicAttraction[]>([])
  const [selectedTourSlug, setSelectedTourSlug] = useState<string | null>(null)
  const [selectedAttraction, setSelectedAttraction] = useState<PublicAttraction | null>(null)
  const [routes, setRoutes] = useState<PublicStarTourRoute[]>([])
  const [favorites, setFavorites] = useState<string[]>(readFavorites)
  const [detailTarget, setDetailTarget] = useState<DetailTarget>(null)
  const [hoveredListAttraction, setHoveredListAttraction] = useState<PublicAttraction | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const searchInputRef = useRef<HTMLInputElement>(null)
  const tourListRef = useRef<HTMLDivElement>(null)

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

  useEffect(() => {
    const mediaQuery = window.matchMedia(MOBILE_TOURISM_QUERY)
    const updateCompactViewport = () => setCompactViewport(mediaQuery.matches)
    updateCompactViewport()
    mediaQuery.addEventListener('change', updateCompactViewport)
    return () => mediaQuery.removeEventListener('change', updateCompactViewport)
  }, [])

  const normalizedQuery = normalizeSearchText(query.trim(), language)
  const filteredTours = useMemo(
    () =>
      tours.filter((tour) =>
        normalizeSearchText(
          `${tour.name} ${tour.shortDescription} ${tour.stops.map((stop) => stop.name).join(' ')}`,
          language,
        ).includes(normalizedQuery),
      ),
    [language, normalizedQuery, tours],
  )
  const categoryAttractions = useMemo(
    () =>
      attractions.filter(
        (attraction) =>
          selectedCategory === 'all' || categoryForAttraction(attraction) === selectedCategory,
      ),
    [attractions, selectedCategory],
  )
  const filteredAttractions = useMemo(
    () =>
      categoryAttractions.filter((attraction) =>
        normalizeSearchText(`${attraction.name} ${attraction.shortDescription}`, language).includes(
          normalizedQuery,
        ),
      ),
    [categoryAttractions, language, normalizedQuery],
  )
  const activeAttraction =
    filteredAttractions.find((attraction) => attraction.slug === selectedAttraction?.slug) ??
    filteredAttractions[0] ??
    null
  const activeTour =
    filteredTours.find((tour) => tour.slug === selectedTourSlug) ?? filteredTours[0] ?? null
  const activeRoute = routes.find((route) => route.tourSlug === activeTour?.slug) ?? null
  const selectedRoute = routes.find((route) => route.tourSlug === selectedTourSlug) ?? null
  const mapSearchActive = compactViewport && layout === 'map' && normalizedQuery.length > 0

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

  function selectAdjacentTour(direction: 1 | -1) {
    if (filteredTours.length < 2 || !activeTour) return
    const index = filteredTours.findIndex((tour) => tour.slug === activeTour.slug)
    const nextTour =
      filteredTours[(index + direction + filteredTours.length) % filteredTours.length]
    if (!nextTour) return
    setMobileCardAnimation(direction === 1 ? 'next' : 'previous')
    setSelectedTourSlug(nextTour.slug)
  }

  function selectAdjacentAttraction(direction: 1 | -1) {
    if (filteredAttractions.length < 2 || !activeAttraction) return
    const index = filteredAttractions.findIndex(
      (attraction) => attraction.slug === activeAttraction.slug,
    )
    const nextAttraction =
      filteredAttractions[
        (index + direction + filteredAttractions.length) % filteredAttractions.length
      ]
    if (!nextAttraction) return
    setMobileCardAnimation(direction === 1 ? 'next' : 'previous')
    selectAttraction(nextAttraction)
  }

  const tourSwipeHandlers = useSwipeable({
    delta: 32,
    onSwipedLeft: () => selectAdjacentTour(1),
    onSwipedRight: () => selectAdjacentTour(-1),
    onSwipedUp: () => {
      if (activeTour) setDetailTarget({ type: 'tour', value: activeTour })
    },
    preventScrollOnSwipe: true,
    swipeDuration: 500,
  })

  const attractionSwipeHandlers = useSwipeable({
    delta: 32,
    onSwipedLeft: () => selectAdjacentAttraction(1),
    onSwipedRight: () => selectAdjacentAttraction(-1),
    preventScrollOnSwipe: true,
    swipeDuration: 500,
  })

  function handleTourListScroll() {
    const list = tourListRef.current
    if (!list) return
    const center = list.scrollLeft + list.clientWidth / 2
    const cards = Array.from(list.querySelectorAll<HTMLElement>('[data-tour-slug]'))
    const closest = cards.reduce<HTMLElement | null>((current, card) => {
      if (!current) return card
      const cardCenter = card.offsetLeft + card.offsetWidth / 2
      const currentCenter = current.offsetLeft + current.offsetWidth / 2
      return Math.abs(cardCenter - center) < Math.abs(currentCenter - center) ? card : current
    }, null)
    if (closest?.dataset.tourSlug) {
      setSelectedTourSlug((current) =>
        current === closest.dataset.tourSlug ? current : (closest.dataset.tourSlug ?? current),
      )
    }
  }

  function scrollToTour(slug: string) {
    setSelectedTourSlug(slug)
    if (window.matchMedia(MOBILE_TOURISM_QUERY).matches) return
    const card = Array.from(tourListRef.current?.children ?? []).find(
      (element) => element instanceof HTMLElement && element.dataset.tourSlug === slug,
    )
    card?.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' })
  }

  function selectAttraction(attraction: PublicAttraction | null) {
    setSelectedAttraction(attraction)
  }

  function updateQuery(value: string) {
    setQuery(value)
  }

  function closeSearch() {
    setQuery('')
    searchInputRef.current?.blur()
  }

  function selectSearchTour(tour: PublicStarTour) {
    setSelectedTourSlug(tour.slug)
    closeSearch()
  }

  function selectSearchAttraction(attraction: PublicAttraction) {
    selectAttraction(attraction)
    closeSearch()
  }

  function handleSearchKeyDown(event: ReactKeyboardEvent<HTMLInputElement>) {
    if (event.key !== 'Enter' || !mapSearchActive) return

    event.preventDefault()
    if (view === 'tours') {
      const firstTour = filteredTours[0]
      if (firstTour) selectSearchTour(firstTour)
      else closeSearch()
      return
    }

    const firstAttraction = filteredAttractions[0]
    if (firstAttraction) selectSearchAttraction(firstAttraction)
    else closeSearch()
  }

  const mapAttractions = useMemo(() => {
    if (view === 'attractions') return mapSearchActive ? categoryAttractions : filteredAttractions

    const matchingStopSlugs = new Set(tours.flatMap((tour) => tour.stops.map((stop) => stop.slug)))
    return attractions.filter((attraction) => matchingStopSlugs.has(attraction.slug))
  }, [attractions, categoryAttractions, filteredAttractions, mapSearchActive, tours, view])
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
    <main
      id="main-content"
      className={`tourism-page${layout === 'list' ? ' tourism-page-list-view' : ''}`}
    >
      <section className="tourism-sidebar" aria-labelledby="tourism-title">
        <div className="tourism-intro">
          <h1 id="tourism-title">{t('tourism.title')}</h1>
        </div>

        <div className="tourism-controls">
          <div className="tourism-view-switch" role="tablist" aria-label={t('tourism.viewLabel')}>
            <button
              type="button"
              role="tab"
              aria-selected={view === 'tours'}
              className={view === 'tours' ? 'active' : ''}
              onClick={() => {
                setHoveredListAttraction(null)
                setView('tours')
              }}
            >
              {t('tourism.tours')}
            </button>
            <button
              type="button"
              role="tab"
              aria-selected={view === 'attractions'}
              className={view === 'attractions' ? 'active' : ''}
              onClick={() => {
                setHoveredListAttraction(null)
                setView('attractions')
              }}
            >
              {t('tourism.attractions')}
            </button>
          </div>
        </div>

        <div className="tourism-search-area">
          <label className="tourism-search">
            <Search aria-hidden="true" size={23} />
            <span className="visually-hidden">{t('tourism.searchLabel')}</span>
            <input
              ref={searchInputRef}
              type="search"
              value={query}
              placeholder={
                view === 'tours' ? t('tourism.searchTours') : t('tourism.searchAttractions')
              }
              onChange={(event) => updateQuery(event.target.value)}
              onKeyDown={handleSearchKeyDown}
            />
          </label>

          <div className="tourism-layout-switch" role="group" aria-label={t('tourism.layoutLabel')}>
            <button
              type="button"
              className={layout === 'map' ? 'active' : ''}
              aria-label={t('tourism.mapView')}
              aria-pressed={layout === 'map'}
              onClick={() => {
                setHoveredListAttraction(null)
                setLayout('map')
              }}
            >
              <MapIcon aria-hidden="true" size={18} />
            </button>
            <button
              type="button"
              className={layout === 'list' ? 'active' : ''}
              aria-label={t('tourism.listView')}
              aria-pressed={layout === 'list'}
              onClick={() => {
                setHoveredListAttraction(null)
                setLayout('list')
                setSelectedCategory('all')
              }}
            >
              <List aria-hidden="true" size={18} />
            </button>
          </div>

          {!loading && !loadError && mapSearchActive && (
            <section className="tourism-search-results" aria-label={t('tourism.searchResults')}>
              {view === 'tours'
                ? filteredTours.map((tour) => (
                    <button key={tour.slug} type="button" onClick={() => selectSearchTour(tour)}>
                      <span>{tour.name}</span>
                      <ChevronRight aria-hidden="true" size={18} />
                    </button>
                  ))
                : filteredAttractions.map((attraction) => (
                    <button
                      key={attraction.slug}
                      type="button"
                      onClick={() => selectSearchAttraction(attraction)}
                    >
                      <span>{attraction.name}</span>
                      <ChevronRight aria-hidden="true" size={18} />
                    </button>
                  ))}
              {(view === 'tours' ? filteredTours : filteredAttractions).length === 0 && (
                <p>{view === 'tours' ? t('tourism.noTours') : t('tourism.noAttractions')}</p>
              )}
            </section>
          )}
        </div>

        {view === 'attractions' && layout === 'map' && (
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

        {!loading && !loadError && view === 'tours' && !mapSearchActive && (
          <div className="tourism-tour-carousel">
            <div
              ref={tourListRef}
              className="tourism-tour-list"
              aria-live="polite"
              onScroll={handleTourListScroll}
            >
              {filteredTours.map((tour) => {
                const selected = tour.slug === activeTour?.slug
                const image = tourImage(tour)
                const previewStops =
                  tour.stops.length > 3 ? [tour.stops[0], null, tour.stops.at(-1)!] : tour.stops
                return (
                  <article
                    key={tour.slug}
                    data-tour-slug={tour.slug}
                    className={`tourism-tour-card${selected ? ' selected' : ''}${selected && mobileCardAnimation ? ` tourism-card-enter-${mobileCardAnimation}` : ''}`}
                    style={{ '--tour-color': tour.mapColor } as CSSProperties}
                    {...(selected ? tourSwipeHandlers : {})}
                  >
                    <button
                      className="tourism-tour-select"
                      type="button"
                      aria-label={`${tour.name} ${t('tourism.selectTour')}`}
                      onClick={() => scrollToTour(tour.slug)}
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
                              <span>
                                {index === previewStops.length - 1 ? tour.stops.length : 1}
                              </span>
                              <span className="tourism-tour-stop-label">{stop.name}</span>
                            </li>
                          ) : (
                            <li key="more" className="tourism-tour-stops-more">
                              <span aria-hidden="true">⋮</span>
                              <span className="tourism-tour-stop-label">
                                {t('tourism.intermediateStops', { count: tour.stops.length - 2 })}
                              </span>
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
              {filteredTours.length === 0 && (
                <p className="tourism-state">{t('tourism.noTours')}</p>
              )}
            </div>
            {filteredTours.length > 1 && (
              <div className="tourism-tour-pagination" aria-label={t('tourism.tourPagination')}>
                {filteredTours.map((tour) => (
                  <button
                    key={tour.slug}
                    type="button"
                    className={tour.slug === activeTour?.slug ? 'active' : ''}
                    aria-label={t('tourism.selectTourPagination', { name: tour.name })}
                    aria-current={tour.slug === activeTour?.slug ? 'true' : undefined}
                    onClick={() => scrollToTour(tour.slug)}
                  />
                ))}
              </div>
            )}
          </div>
        )}

        {!loading && !loadError && view === 'attractions' && !mapSearchActive && (
          <section className="tourism-attraction-section" aria-labelledby="attractions-heading">
            <div className="tourism-list-heading">
              <h2 id="attractions-heading">{t('tourism.attractions')}</h2>
              <p>{t('tourism.placeCount', { count: filteredAttractions.length })}</p>
            </div>
            <div className="tourism-attraction-list">
              {filteredAttractions.map((attraction) => {
                const image = attractionImage(attraction)
                return (
                  <article
                    key={attraction.slug}
                    className={`tourism-attraction-card${image ? ' has-image' : ''}${attraction.slug === activeAttraction?.slug ? ' selected' : ''}${attraction.slug === activeAttraction?.slug && mobileCardAnimation ? ` tourism-card-enter-${mobileCardAnimation}` : ''}`}
                    {...(attraction.slug === activeAttraction?.slug ? attractionSwipeHandlers : {})}
                    onMouseEnter={() => {
                      if (!compactViewport && layout === 'map') setHoveredListAttraction(attraction)
                    }}
                    onMouseLeave={() => setHoveredListAttraction(null)}
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
            {filteredAttractions.length > 1 && (
              <div
                className="tourism-attraction-pagination"
                aria-label={t('tourism.attractionPagination')}
              >
                {filteredAttractions.map((attraction) => (
                  <button
                    key={attraction.slug}
                    type="button"
                    className={attraction.slug === activeAttraction?.slug ? 'active' : ''}
                    aria-label={t('tourism.selectAttractionPagination', { name: attraction.name })}
                    aria-current={attraction.slug === activeAttraction?.slug ? 'true' : undefined}
                    onClick={() => selectAttraction(attraction)}
                  />
                ))}
              </div>
            )}
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
            selectedAttraction={
              compactViewport && view === 'attractions'
                ? mapSearchActive
                  ? selectedAttraction
                  : activeAttraction
                : compactViewport
                  ? selectedAttraction
                  : hoveredListAttraction
            }
            focusAttraction={
              layout === 'map' && view === 'attractions' && !mapSearchActive
                ? compactViewport
                  ? activeAttraction
                  : hoveredListAttraction
                : null
            }
            focusZoom={
              compactViewport ? MOBILE_ATTRACTION_FOCUS_ZOOM : DESKTOP_ATTRACTION_FOCUS_ZOOM
            }
            selectedRoute={
              view === 'tours' ? (mapSearchActive ? selectedRoute : activeRoute) : null
            }
            routes={view === 'tours' ? mapRoutes : []}
            visible={layout === 'map'}
            onSelectAttraction={(attraction) => {
              if (compactViewport) selectAttraction(attraction)
            }}
            onOpenAttractionDetails={(attraction) =>
              setDetailTarget({ type: 'attraction', value: attraction })
            }
          />
        )}
        {view === 'tours' && activeTour && !activeRoute && (
          <p className="tourism-route-status">
            {t(`tourism.routeStatus.${activeTour.routeStatus}`)}
          </p>
        )}
      </section>
      <TourismDetailsModal
        detail={detailTarget}
        route={
          detailTarget?.type === 'tour'
            ? (routes.find((route) => route.tourSlug === detailTarget.value.slug) ?? null)
            : null
        }
        onHide={() => setDetailTarget(null)}
      />
    </main>
  )
}
