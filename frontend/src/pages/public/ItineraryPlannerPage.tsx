import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { SectionHeader } from '../../components/SectionHeader'
import { guesthouses } from '../../data/demoCatalog'

export function ItineraryPlannerPage() {
  const { t } = useTranslation()

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    event.currentTarget.reset()
  }

  return (
    <section className="page">
      <SectionHeader title={t('itinerary.title')} lead={t('itinerary.lead')} />
      <form className="form-panel form-grid" onSubmit={handleSubmit}>
        <label>
          {t('itinerary.interests')}
          <select name="interests" multiple required>
            <option value="nature">Természet</option>
            <option value="history">Történelem</option>
            <option value="family">Családbarát</option>
            <option value="active">Aktív program</option>
          </select>
        </label>
        <label>
          {t('itinerary.fitness')}
          <select name="fitness" required>
            <option value="easy">Könnyű</option>
            <option value="medium">Közepes</option>
            <option value="active">Aktív</option>
          </select>
        </label>
        <label>
          {t('itinerary.hours')}
          <input name="hours" type="number" min="1" max="12" defaultValue="6" required />
        </label>
        <label>
          {t('itinerary.departure')}
          <select name="departure" required>
            {guesthouses.map((guesthouse) => (
              <option key={guesthouse.slug} value={guesthouse.slug}>
                {guesthouse.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          {t('itinerary.transport')}
          <select name="transport" required>
            <option value="car">Autó</option>
            <option value="walk">Gyalog</option>
            <option value="mixed">Vegyes</option>
          </select>
        </label>
        <label>
          {t('itinerary.pace')}
          <select name="pace" required>
            <option value="calm">Nyugodt</option>
            <option value="balanced">Kiegyensúlyozott</option>
            <option value="dense">Sűrű</option>
          </select>
        </label>
        <label className="full-field">
          {t('itinerary.language')}
          <select name="language" required>
            <option value="hu">HU</option>
            <option value="ro">RO</option>
            <option value="en">EN</option>
          </select>
        </label>
        <div className="full-field">
          <button className="button-action" type="submit">
            {t('itinerary.submit')}
          </button>
        </div>
      </form>
    </section>
  )
}
