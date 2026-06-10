import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'
import { guesthouses, rooms } from '../../data/demoCatalog'

export function BookingRequestPage() {
  const { t } = useTranslation()

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    event.currentTarget.reset()
  }

  return (
    <section className="page">
      <SectionHeader title={t('booking.title')} lead={t('booking.lead')} />
      <form className="form-panel form-grid" onSubmit={handleSubmit}>
        <label>
          {t('booking.guesthouse')}
          <select name="guesthouse" required>
            {guesthouses.map((guesthouse) => (
              <option key={guesthouse.slug} value={guesthouse.slug}>
                {guesthouse.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          {t('booking.room')}
          <select name="room" required>
            {rooms.map((room) => (
              <option key={room.slug} value={room.slug}>
                {room.propertyName} - {t(room.nameKey)}
              </option>
            ))}
          </select>
        </label>
        <label>
          {t('booking.name')}
          <input name="name" autoComplete="name" required />
        </label>
        <label>
          {t('booking.email')}
          <input name="email" type="email" autoComplete="email" required />
        </label>
        <label>
          {t('booking.arrival')}
          <input name="arrival" type="date" required />
        </label>
        <label>
          {t('booking.departure')}
          <input name="departure" type="date" required />
        </label>
        <label>
          {t('booking.guests')}
          <input name="guests" type="number" min="1" defaultValue="2" required />
        </label>
        <label>
          {t('booking.language')}
          <select name="language" required>
            <option value="hu">HU</option>
            <option value="ro">RO</option>
            <option value="en">EN</option>
          </select>
        </label>
        <label className="full-field">
          {t('booking.message')}
          <textarea name="message" />
        </label>
        <div className="full-field">
          <button className="button-action" type="submit">
            {t('booking.submit')}
          </button>
        </div>
      </form>
    </section>
  )
}
