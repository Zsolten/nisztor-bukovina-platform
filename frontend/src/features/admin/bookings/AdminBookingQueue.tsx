import {
  AlertTriangle,
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Filter,
  Inbox,
  RefreshCw,
  Search,
  Users,
} from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchGuesthouses, type GuesthouseSummary } from '../../../shared/api/guesthouses'
import { useAdminAuth } from '../auth/adminAuthContext'
import {
  fetchAdminBookings,
  type AdminBookingFilters,
  type AdminBookingPage,
  type AdminBookingSortDirection,
  type AdminBookingSortField,
  type AdminBookingStatus,
} from '../api/adminBookings'

const STATUS_LABELS: Record<AdminBookingStatus, string> = {
  RECEIVED: 'Beérkezett',
  UNDER_REVIEW: 'Ellenőrzés alatt',
  CONFIRMED: 'Visszaigazolt',
  REJECTED: 'Elutasított',
  CANCELLED: 'Lemondott',
}

type QueueState =
  | { status: 'loading'; data: null }
  | { status: 'error'; data: null }
  | { status: 'success'; data: AdminBookingPage }

const initialFilters: AdminBookingFilters = { guesthouseId: '', search: '', status: '' }
const initialSort: { field: AdminBookingSortField; direction: AdminBookingSortDirection } = {
  field: 'checkInDate',
  direction: 'asc',
}
const dateFormatter = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: 'short',
  day: 'numeric',
})
const dateTimeFormatter = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: 'short',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

export default function AdminBookingQueue() {
  const { authorizedFetch } = useAdminAuth()
  const [filters, setFilters] = useState(initialFilters)
  const [searchInput, setSearchInput] = useState('')
  const [guesthouses, setGuesthouses] = useState<GuesthouseSummary[]>([])
  const [page, setPage] = useState(0)
  const [sort, setSort] = useState(initialSort)
  const [retryKey, setRetryKey] = useState(0)
  const [queue, setQueue] = useState<QueueState>({ status: 'loading', data: null })
  const [isRefreshing, setIsRefreshing] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    void fetchGuesthouses('hu', controller.signal)
      .then((data) => setGuesthouses(Array.isArray(data) ? data : []))
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) setGuesthouses([])
      })
    return () => controller.abort()
  }, [])

  useEffect(() => {
    const search = searchInput.trim()
    if (search === filters.search) return undefined

    const timeout = window.setTimeout(() => {
      setIsRefreshing(true)
      setFilters((current) => ({ ...current, search }))
      setPage(0)
    }, 350)
    return () => window.clearTimeout(timeout)
  }, [filters.search, searchInput])

  useEffect(() => {
    const controller = new AbortController()
    void fetchAdminBookings(
      authorizedFetch,
      filters,
      page,
      sort.field,
      sort.direction,
      controller.signal,
    )
      .then((data) => {
        setQueue({ status: 'success', data })
        setIsRefreshing(false)
      })
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setQueue({ status: 'error', data: null })
          setIsRefreshing(false)
        }
      })
    return () => controller.abort()
  }, [authorizedFetch, filters, page, retryKey, sort])

  const visibleGuesthouses = useMemo(() => {
    if (guesthouses.length > 0) {
      return guesthouses.map(({ id, name }) => ({ id, name }))
    }
    if (queue.status !== 'success') return []
    return Array.from(
      new Map(
        queue.data.content.map((booking) => [booking.guesthouseId, booking.guesthouseName]),
      ).entries(),
      ([id, name]) => ({ id, name }),
    )
  }, [guesthouses, queue])

  function updateFilter(field: keyof AdminBookingFilters, value: string) {
    setIsRefreshing(true)
    setFilters((current) => ({ ...current, [field]: value }))
    setPage(0)
  }

  function changePage(nextPage: number) {
    if (nextPage === page) return
    setIsRefreshing(true)
    setPage(nextPage)
  }

  function retry() {
    setQueue({ status: 'loading', data: null })
    setRetryKey((current) => current + 1)
  }

  function updateSort(field: AdminBookingSortField) {
    setIsRefreshing(true)
    setSort((current) => ({
      field,
      direction:
        current.field === field
          ? current.direction === 'asc'
            ? 'desc'
            : 'asc'
          : field === 'createdAt'
            ? 'desc'
            : 'asc',
    }))
    setPage(0)
  }

  function sortIcon(field: AdminBookingSortField) {
    if (sort.field !== field) return <ArrowUpDown aria-hidden="true" size={14} />
    return sort.direction === 'asc' ? (
      <ArrowUp aria-hidden="true" size={14} />
    ) : (
      <ArrowDown aria-hidden="true" size={14} />
    )
  }

  return (
    <section className="admin-booking-queue" aria-labelledby="booking-queue-title">
      <header className="admin-booking-heading">
        <div>
          <p className="admin-eyebrow">Napi munkalista</p>
          <h1 id="booking-queue-title">Foglalási kérelmek</h1>
          <p>Alapértelmezetten a legközelebbi érkezések jelennek meg felül.</p>
        </div>
        {queue.status === 'success' && (
          <p className="admin-booking-count">
            <strong>{queue.data.totalElements}</strong>
            <span>kérelem</span>
          </p>
        )}
      </header>

      <div className="admin-booking-filters" aria-label="Foglalási kérelmek szűrése">
        <Filter aria-hidden="true" size={18} />
        <label className="admin-booking-search">
          <span>Keresés</span>
          <span className="admin-booking-search-control">
            <Search aria-hidden="true" size={17} />
            <input
              type="search"
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Foglalási szám vagy név"
            />
          </span>
        </label>
        <label>
          <span>Panzió</span>
          <select
            value={filters.guesthouseId}
            onChange={(event) => updateFilter('guesthouseId', event.target.value)}
          >
            <option value="">Minden panzió</option>
            {visibleGuesthouses.map((guesthouse) => (
              <option key={guesthouse.id} value={guesthouse.id}>
                {guesthouse.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          <span>Állapot</span>
          <select
            value={filters.status}
            onChange={(event) => updateFilter('status', event.target.value)}
          >
            <option value="">Minden állapot</option>
            {Object.entries(STATUS_LABELS).map(([status, label]) => (
              <option key={status} value={status}>
                {label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {queue.status === 'loading' && (
        <div className="admin-booking-state" role="status">
          <RefreshCw className="admin-booking-spinner" aria-hidden="true" size={24} />
          <strong>Foglalások betöltése</strong>
        </div>
      )}

      {queue.status === 'error' && (
        <div className="admin-booking-state admin-booking-state-error" role="alert">
          <AlertTriangle aria-hidden="true" size={26} />
          <strong>Nem sikerült betölteni a kérelmeket.</strong>
          <span>Ellenőrizze a kapcsolatot, majd próbálja újra.</span>
          <button type="button" onClick={retry}>
            <RefreshCw aria-hidden="true" size={16} />
            Újrapróbálás
          </button>
        </div>
      )}

      {queue.status === 'success' && queue.data.content.length === 0 && (
        <div className="admin-booking-state">
          <Inbox aria-hidden="true" size={28} />
          <strong>Nincs találat</strong>
          <span>A kiválasztott szűrőknek egyetlen foglalási kérelem sem felel meg.</span>
        </div>
      )}

      {queue.status === 'success' && queue.data.content.length > 0 && (
        <>
          <div
            className="admin-booking-list"
            aria-busy={isRefreshing}
            aria-label="Foglalási kérelmek"
          >
            {isRefreshing && (
              <span className="visually-hidden" role="status">
                Lista frissítése
              </span>
            )}
            <div className="admin-booking-list-header">
              <span>Kérelem</span>
              <button type="button" onClick={() => updateSort('checkInDate')}>
                Tartózkodás {sortIcon('checkInDate')}
              </button>
              <span>Vendégek</span>
              <button type="button" onClick={() => updateSort('totalPayable')}>
                Összeg {sortIcon('totalPayable')}
              </button>
              <span>Állapot</span>
              <button type="button" onClick={() => updateSort('createdAt')}>
                Beérkezett {sortIcon('createdAt')}
              </button>
            </div>
            {queue.data.content.map((booking) => (
              <Link
                className="admin-booking-row"
                key={booking.id}
                to={`/admin/bookings/${booking.id}`}
              >
                <span className="admin-booking-primary" data-label="Kérelem">
                  <strong>{booking.publicReference}</strong>
                  <small>{booking.guesthouseName}</small>
                </span>
                <span className="admin-booking-stay" data-label="Tartózkodás">
                  <CalendarDays aria-hidden="true" size={16} />
                  <span>
                    {dateFormatter.format(new Date(`${booking.checkInDate}T00:00:00`))} –{' '}
                    {dateFormatter.format(new Date(`${booking.checkOutDate}T00:00:00`))}
                  </span>
                  <small>{booking.nights} éjszaka</small>
                </span>
                <span className="admin-booking-guests" data-label="Vendégek">
                  <Users aria-hidden="true" size={16} />
                  {booking.totalGuests} fő
                </span>
                <strong className="admin-booking-total" data-label="Összeg">
                  {new Intl.NumberFormat('hu-HU', {
                    style: 'currency',
                    currency: booking.currency,
                    maximumFractionDigits: 0,
                  }).format(booking.totalPayable)}
                </strong>
                <span
                  className={`admin-booking-status admin-booking-status-${booking.status.toLowerCase().replace('_', '-')}`}
                  data-label="Állapot"
                >
                  {STATUS_LABELS[booking.status]}
                </span>
                <time data-label="Beérkezett" dateTime={booking.createdAt}>
                  {dateTimeFormatter.format(new Date(booking.createdAt))}
                </time>
              </Link>
            ))}
          </div>

          <nav className="admin-booking-pagination" aria-label="Foglalási lista lapozása">
            <button type="button" disabled={page === 0} onClick={() => changePage(page - 1)}>
              <ChevronLeft aria-hidden="true" size={17} />
              Előző
            </button>
            <div className="admin-booking-page-numbers">
              {Array.from({ length: queue.data.totalPages }, (_, index) => index).map(
                (pageNumber) => (
                  <button
                    aria-current={pageNumber === page ? 'page' : undefined}
                    className={pageNumber === page ? 'active' : undefined}
                    key={pageNumber}
                    type="button"
                    onClick={() => changePage(pageNumber)}
                  >
                    {pageNumber + 1}
                  </button>
                ),
              )}
            </div>
            <button
              type="button"
              disabled={page + 1 >= queue.data.totalPages}
              onClick={() => changePage(page + 1)}
            >
              Következő
              <ChevronRight aria-hidden="true" size={17} />
            </button>
          </nav>
        </>
      )}
    </section>
  )
}
