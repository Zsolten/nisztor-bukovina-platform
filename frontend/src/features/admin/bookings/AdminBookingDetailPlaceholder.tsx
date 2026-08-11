import { ArrowLeft } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'

export default function AdminBookingDetailPlaceholder() {
  const { bookingId } = useParams()

  return (
    <section
      className="admin-booking-detail-placeholder"
      aria-labelledby="admin-booking-detail-title"
    >
      <Link to="/admin/bookings">
        <ArrowLeft aria-hidden="true" size={17} />
        Vissza a foglalási kérelmekhez
      </Link>
      <p className="admin-eyebrow">Foglalási kérelem</p>
      <h1 id="admin-booking-detail-title">Kérelem részletei</h1>
      <code>{bookingId}</code>
    </section>
  )
}
