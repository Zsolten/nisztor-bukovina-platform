import {
  AlertTriangle,
  ArrowLeft,
  BedDouble,
  CalendarDays,
  Check,
  CircleDollarSign,
  Clock3,
  Mail,
  MapPin,
  Phone,
  Printer,
  RefreshCw,
  Save,
  Users,
  Utensils,
} from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Modal } from 'react-bootstrap'
import { Link, useParams } from 'react-router-dom'
import {
  AdminBookingApiError,
  fetchAdminBookingDetail,
  updateAdminBookingInternalNote,
  updateAdminBookingStatus,
  type AdminBookingDetail as AdminBookingDetailData,
  type AdminBookingStatus,
} from '../api/adminBookings'
import { useAdminAuth } from '../auth/adminAuthContext'

const STATUS_LABELS: Record<AdminBookingStatus, string> = {
  RECEIVED: 'Beérkezett',
  UNDER_REVIEW: 'Ellenőrzés alatt',
  CONFIRMED: 'Visszaigazolt',
  REJECTED: 'Elutasított',
  CANCELLED: 'Lemondott',
}

const ERROR_MESSAGES: Record<string, string> = {
  ADMIN_BOOKING_NOT_FOUND: 'A foglalási kérelem nem található.',
  INVALID_BOOKING_STATUS_TRANSITION: 'Ez az állapotváltás már nem hajtható végre.',
  INTERNAL_NOTE_REQUIRED: 'Adjon meg belső megjegyzést, vagy törölje a mező tartalmát.',
  INTERNAL_NOTE_TOO_LONG: 'A belső megjegyzés legfeljebb 4000 karakter lehet.',
  INVALID_ADMIN_BOOKING_REQUEST: 'A módosítás nem menthető. Ellenőrizze a megadott adatokat.',
}

const dateFormatter = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
})
const dateTimeFormatter = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: 'short',
  day: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})
const weekdayFormatter = new Intl.DateTimeFormat('hu-HU', { weekday: 'long' })

type DetailState =
  | { status: 'loading' }
  | { status: 'error'; notFound: boolean }
  | { status: 'success'; data: AdminBookingDetailData }

export default function AdminBookingDetail() {
  const { bookingId } = useParams()
  const { authorizedFetch } = useAdminAuth()
  const [detail, setDetail] = useState<DetailState>({ status: 'loading' })
  const [internalNote, setInternalNote] = useState('')
  const [pendingStatus, setPendingStatus] = useState<AdminBookingStatus | null>(null)
  const [saving, setSaving] = useState<'status' | 'note' | null>(null)
  const [feedback, setFeedback] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const loadDetail = useCallback(
    async (signal?: AbortSignal) => {
      if (!bookingId) {
        setDetail({ status: 'error', notFound: true })
        return
      }
      try {
        const data = await fetchAdminBookingDetail(authorizedFetch, bookingId, signal)
        setDetail({ status: 'success', data })
        setInternalNote(data.internalNote ?? '')
      } catch (caught) {
        if (caught instanceof DOMException && caught.name === 'AbortError') return
        setDetail({
          status: 'error',
          notFound: caught instanceof AdminBookingApiError && caught.status === 404,
        })
      }
    },
    [authorizedFetch, bookingId],
  )

  useEffect(() => {
    const controller = new AbortController()
    if (!bookingId) return undefined

    void fetchAdminBookingDetail(authorizedFetch, bookingId, controller.signal)
      .then((data) => {
        setDetail({ status: 'success', data })
        setInternalNote(data.internalNote ?? '')
      })
      .catch((caught: unknown) => {
        if (caught instanceof DOMException && caught.name === 'AbortError') return
        setDetail({
          status: 'error',
          notFound: caught instanceof AdminBookingApiError && caught.status === 404,
        })
      })
    return () => controller.abort()
  }, [authorizedFetch, bookingId])

  const totalGuests = useMemo(() => {
    if (detail.status !== 'success') return 0
    const { adults, childrenAge3to10, childrenAge0to3 } = detail.data.stay
    return adults + childrenAge3to10 + childrenAge0to3
  }, [detail])

  async function changeStatus(status: AdminBookingStatus) {
    if (!bookingId) return
    setPendingStatus(null)
    setSaving('status')
    setError(null)
    setFeedback(null)
    try {
      if (
        detail.status === 'success' &&
        detail.data.status === 'RECEIVED' &&
        (status === 'CONFIRMED' || status === 'REJECTED')
      ) {
        await updateAdminBookingStatus(authorizedFetch, bookingId, 'UNDER_REVIEW')
      }
      await updateAdminBookingStatus(authorizedFetch, bookingId, status)
      await loadDetail()
      setFeedback(`Az állapot frissült: ${STATUS_LABELS[status]}.`)
    } catch (caught) {
      setError(operationError(caught))
    } finally {
      setSaving(null)
    }
  }

  async function saveInternalNote() {
    if (!bookingId || internalNote.length > 4000) return
    setSaving('note')
    setError(null)
    setFeedback(null)
    try {
      await updateAdminBookingInternalNote(authorizedFetch, bookingId, internalNote)
      await loadDetail()
      setFeedback('A belső megjegyzés mentve.')
    } catch (caught) {
      setError(operationError(caught))
    } finally {
      setSaving(null)
    }
  }

  if (detail.status === 'loading') {
    return (
      <DetailState
        icon={<RefreshCw className="admin-booking-spinner" />}
        text="Kérelem betöltése"
      />
    )
  }

  if (detail.status === 'error') {
    return (
      <DetailState
        icon={<AlertTriangle />}
        text={
          detail.notFound
            ? 'A foglalási kérelem nem található.'
            : 'Nem sikerült betölteni a kérelmet.'
        }
        action={
          detail.notFound ? undefined : (
            <button type="button" onClick={() => void loadDetail()}>
              <RefreshCw aria-hidden="true" size={16} /> Újrapróbálás
            </button>
          )
        }
      />
    )
  }

  const booking = detail.data
  const price = booking.priceSnapshot
  const noteChanged = internalNote.trim() !== (booking.internalNote ?? '')
  const roomSummary = booking.rooms.map((room) => room.roomTypeName).join(', ')
  const roomQuantities = booking.rooms
    .map((room) => `${room.quantity} db ${room.roomTypeName}`)
    .join(' · ')
  const selectedMeals = [
    booking.services.breakfastParticipants > 0
      ? { label: 'Reggeli', participants: booking.services.breakfastParticipants }
      : null,
    booking.services.dinnerParticipants > 0
      ? { label: 'Vacsora', participants: booking.services.dinnerParticipants }
      : null,
  ].filter((meal): meal is { label: string; participants: number } => meal !== null)
  const mealSummary = selectedMeals.map((meal) => meal.label).join(' és ')
  const mealParticipants = selectedMeals
    .map((meal) => `${meal.participants} fő ${meal.label.toLowerCase()}`)
    .join(' · ')

  return (
    <section className="admin-booking-detail" aria-labelledby="admin-booking-detail-title">
      <div className="admin-booking-toolbar">
        <Link className="admin-booking-back" to="/admin/bookings">
          <ArrowLeft aria-hidden="true" size={17} /> Vissza a foglalásokhoz
        </Link>
        <div>
          <span>Kérelem {booking.publicReference}</span>
          <button type="button" onClick={() => window.print()}>
            <Printer aria-hidden="true" size={16} />
            Nyomtatás
          </button>
        </div>
      </div>

      <header className="admin-booking-detail-heading">
        <div>
          <div className="admin-booking-title-row">
            <h1 id="admin-booking-detail-title">{booking.publicReference}</h1>
            <span
              className={`admin-booking-status admin-booking-status-${statusClass(booking.status)}`}
            >
              {STATUS_LABELS[booking.status]}
            </span>
          </div>
          <p>{booking.guesthouse.name}</p>
        </div>
      </header>

      {(feedback || error) && (
        <div
          className={`admin-booking-feedback ${error ? 'is-error' : 'is-success'}`}
          role={error ? 'alert' : 'status'}
        >
          {error ? (
            <AlertTriangle aria-hidden="true" size={18} />
          ) : (
            <Check aria-hidden="true" size={18} />
          )}
          {error ?? feedback}
        </div>
      )}

      <div className="admin-booking-detail-layout">
        <div className="admin-booking-detail-content">
          <section
            className="admin-detail-card admin-detail-overview"
            aria-labelledby="stay-heading"
          >
            <h2 id="stay-heading">Tartózkodás részletei</h2>
            <DetailItem
              icon={<CalendarDays />}
              label="Érkezés"
              value={formatDate(booking.stay.checkInDate)}
              detail={formatWeekday(booking.stay.checkInDate)}
            />
            <DetailItem
              icon={<CalendarDays />}
              label="Távozás"
              value={formatDate(booking.stay.checkOutDate)}
              detail={formatWeekday(booking.stay.checkOutDate)}
            />
            <DetailItem
              icon={<Clock3 />}
              label="Időtartam"
              value={`${booking.stay.nights} éjszaka`}
            />
            <DetailItem icon={<Users />} label="Vendégek" value={`${totalGuests} fő`} />
            <DetailItem
              icon={<BedDouble />}
              label="Szobák"
              value={roomSummary}
              detail={roomQuantities}
            />
            <DetailItem
              icon={<Utensils />}
              label="Étkezések"
              value={mealSummary || 'Nincs kiválasztva'}
              detail={mealParticipants || undefined}
            />
            <dl className="admin-detail-breakdown">
              <div>
                <dt>Felnőtt</dt>
                <dd>{booking.stay.adults} fő</dd>
              </div>
              <div>
                <dt>Gyermek 3–10 év</dt>
                <dd>{booking.stay.childrenAge3to10} fő</dd>
              </div>
              <div>
                <dt>Gyermek 0–3 év</dt>
                <dd>{booking.stay.childrenAge0to3} fő</dd>
              </div>
            </dl>
          </section>

          <section className="admin-detail-card" aria-labelledby="contact-heading">
            <h2 id="contact-heading">Vendég kapcsolata</h2>
            <div className="admin-detail-contact">
              <span className="admin-detail-contact-avatar" aria-hidden="true">
                {booking.contact.name.charAt(0).toUpperCase()}
              </span>
              <div>
                <strong className="admin-detail-contact-name">{booking.contact.name}</strong>
                <div className="admin-detail-contact-links">
                  <a href={`mailto:${booking.contact.email}`}>
                    <Mail aria-hidden="true" size={17} />
                    {booking.contact.email}
                  </a>
                  <a href={`tel:${booking.contact.phone}`}>
                    <Phone aria-hidden="true" size={17} />
                    {booking.contact.phone}
                  </a>
                </div>
              </div>
              <p>
                <span>Kapcsolattartás nyelve</span>
                <strong>{languageLabel(booking.contact.preferredLanguage)}</strong>
              </p>
            </div>
            <div className="admin-detail-guest-note">
              <span>Vendég megjegyzése</span>
              <p>{booking.guestNote || 'Nem adott meg megjegyzést.'}</p>
            </div>
          </section>

          <section className="admin-detail-card admin-detail-price" aria-labelledby="price-heading">
            <h2 id="price-heading">Árkalkuláció részletei</h2>
            <dl className="admin-price-guests">
              <div>
                <dt>
                  Felnőttek
                  <small>
                    {booking.stay.adults} fő × {booking.stay.nights} éj
                  </small>
                </dt>
                <dd>{formatMoney(price.adultAccommodationTotal, price.currency)}</dd>
              </div>
              <div>
                <dt>
                  Gyermekek 3–10 év
                  <small>
                    {booking.stay.childrenAge3to10} fő × {booking.stay.nights} éj · 25% kedvezmény
                  </small>
                </dt>
                <dd>{formatMoney(price.childAccommodationTotal, price.currency)}</dd>
              </div>
              <div>
                <dt>
                  Gyermekek 0–3 év
                  <small>{booking.stay.childrenAge0to3} fő · ingyenes</small>
                </dt>
                <dd>{formatMoney(0, price.currency)}</dd>
              </div>
            </dl>
            <PriceRow
              label="Szállásdíj"
              value={price.accommodationTotal}
              currency={price.currency}
            />
            {price.singleRoomSurcharge > 0 && (
              <PriceRow
                label="Egyágyas felár"
                value={price.singleRoomSurcharge}
                currency={price.currency}
              />
            )}
            <PriceRow label="Reggeli" value={price.breakfastTotal} currency={price.currency} />
            <PriceRow label="Vacsora" value={price.dinnerTotal} currency={price.currency} />
            <PriceRow total label="Összesen" value={price.totalPayable} currency={price.currency} />
          </section>

          <section className="admin-detail-card" aria-labelledby="history-heading">
            <h2 id="history-heading">Kérelem előzményei</h2>
            <ol className="admin-status-history">
              {booking.statusHistory.map((entry, index) => (
                <li key={`${entry.changedAt}-${index}`}>
                  <span aria-hidden="true" />
                  <div>
                    <strong>{STATUS_LABELS[entry.status]}</strong>
                    <small>{actorLabel(entry.changedBy)}</small>
                  </div>
                  <time dateTime={entry.changedAt}>
                    {dateTimeFormatter.format(new Date(entry.changedAt))}
                  </time>
                </li>
              ))}
            </ol>
          </section>
        </div>

        <aside className="admin-booking-detail-sidebar">
          <section className="admin-decision-summary" aria-labelledby="decision-heading">
            <h2 id="decision-heading">Döntési összegzés</h2>
            <dl>
              <div>
                <dt>Állapot</dt>
                <dd>
                  <span
                    className={`admin-booking-status admin-booking-status-${statusClass(booking.status)}`}
                  >
                    {STATUS_LABELS[booking.status]}
                  </span>
                </dd>
              </div>
              <div>
                <dt>Beérkezett</dt>
                <dd>{dateTimeFormatter.format(new Date(booking.createdAt))}</dd>
              </div>
              <div>
                <dt>Frissítve</dt>
                <dd>{dateTimeFormatter.format(new Date(booking.updatedAt))}</dd>
              </div>
            </dl>
          </section>

          <section className="admin-decision-total" aria-label="Foglalási összeg">
            <span>
              <CircleDollarSign aria-hidden="true" size={18} />
              Foglalási összeg
            </span>
            <strong>{formatMoney(price.totalPayable, price.currency)}</strong>
            <p>A foglalási kérelem mentett végösszege.</p>
          </section>

          <section
            className="admin-detail-card admin-detail-actions"
            aria-labelledby="actions-heading"
          >
            <h2 id="actions-heading">Döntés</h2>
            <StatusActions
              disabled={saving !== null}
              status={booking.status}
              onAction={(status) => {
                if (status === 'CONFIRMED' || status === 'REJECTED') setPendingStatus(status)
                else void changeStatus(status)
              }}
            />
          </section>

          <section className="admin-detail-card admin-detail-note" aria-labelledby="note-heading">
            <h2 id="note-heading">Belső megjegyzés</h2>
            <p>Csak az adminisztráció számára látható.</p>
            <textarea
              aria-label="Belső megjegyzés"
              maxLength={4000}
              rows={6}
              value={internalNote}
              onChange={(event) => setInternalNote(event.target.value)}
            />
            <div>
              <small>{internalNote.length}/4000</small>
              <button
                type="button"
                disabled={!noteChanged || saving !== null}
                onClick={() => void saveInternalNote()}
              >
                <Save aria-hidden="true" size={16} />
                {saving === 'note' ? 'Mentés…' : 'Mentés'}
              </button>
            </div>
          </section>

          <section className="admin-detail-metadata" aria-label="Kérelem metaadatai">
            <p>
              <MapPin aria-hidden="true" size={15} />
              {booking.guesthouse.name}
            </p>
            <p>A kérelem döntésig nem számít visszaigazolt foglalásnak.</p>
          </section>
        </aside>
      </div>

      <Modal centered show={pendingStatus !== null} onHide={() => setPendingStatus(null)}>
        <Modal.Header closeButton>
          <Modal.Title>
            {pendingStatus === 'CONFIRMED' ? 'Kérelem visszaigazolása' : 'Kérelem elutasítása'}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Biztosan {pendingStatus === 'CONFIRMED' ? 'visszaigazolja' : 'elutasítja'} a(z){' '}
          <strong>{booking.publicReference}</strong> kérelmet?
        </Modal.Body>
        <Modal.Footer>
          <Button variant="outline-secondary" onClick={() => setPendingStatus(null)}>
            Mégse
          </Button>
          <Button
            className={
              pendingStatus === 'REJECTED' ? 'admin-action-reject' : 'admin-action-confirm'
            }
            onClick={() => pendingStatus && void changeStatus(pendingStatus)}
          >
            {pendingStatus === 'CONFIRMED' ? 'Visszaigazolás' : 'Elutasítás'}
          </Button>
        </Modal.Footer>
      </Modal>
    </section>
  )
}

function DetailState({
  icon,
  text,
  action,
}: {
  icon: React.ReactNode
  text: string
  action?: React.ReactNode
}) {
  return (
    <div className="admin-booking-state" role="status">
      {icon}
      <strong>{text}</strong>
      {action}
    </div>
  )
}

function DetailItem({
  icon,
  label,
  value,
  detail,
}: {
  icon: React.ReactNode
  label: string
  value: string
  detail?: string
}) {
  return (
    <div className="admin-detail-item">
      {icon}
      <span>{label}</span>
      <strong>{value}</strong>
      {detail && <small>{detail}</small>}
    </div>
  )
}

function PriceRow({
  label,
  value,
  currency,
  total = false,
}: {
  label: string
  value: number
  currency: string
  total?: boolean
}) {
  return (
    <div className={total ? 'admin-price-total' : undefined}>
      <span>{label}</span>
      <strong>{formatMoney(value, currency)}</strong>
    </div>
  )
}

function StatusActions({
  status,
  disabled,
  onAction,
}: {
  status: AdminBookingStatus
  disabled: boolean
  onAction: (status: AdminBookingStatus) => void
}) {
  if (status === 'RECEIVED' || status === 'UNDER_REVIEW')
    return (
      <div>
        <button
          className="admin-action-confirm"
          type="button"
          disabled={disabled}
          onClick={() => onAction('CONFIRMED')}
        >
          Visszaigazolás
        </button>
        <button
          className="admin-action-reject"
          type="button"
          disabled={disabled}
          onClick={() => onAction('REJECTED')}
        >
          Elutasítás
        </button>
      </div>
    )
  return <p className="admin-action-complete">Ehhez az állapothoz nincs további művelet.</p>
}

function operationError(error: unknown) {
  if (error instanceof AdminBookingApiError && error.code)
    return (
      ERROR_MESSAGES[error.code] ??
      'A módosítás nem sikerült. Frissítse az oldalt, majd próbálja újra.'
    )
  return 'A módosítás nem sikerült. Ellenőrizze a kapcsolatot, majd próbálja újra.'
}

function formatDate(value: string) {
  return dateFormatter.format(new Date(`${value}T00:00:00`))
}
function formatWeekday(value: string) {
  return weekdayFormatter.format(new Date(`${value}T00:00:00`))
}
function formatMoney(value: number, currency: string) {
  return new Intl.NumberFormat('hu-HU', {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(value)
}
function statusClass(status: AdminBookingStatus) {
  return status.toLowerCase().replace('_', '-')
}
function languageLabel(language: 'hu' | 'ro' | 'en') {
  return { hu: 'magyar', ro: 'román', en: 'angol' }[language]
}
function actorLabel(actor: string) {
  return actor.replace(/^ADMIN:/, '') || 'Rendszer'
}
