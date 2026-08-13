import { useTranslation } from 'react-i18next'
import type { GuesthouseHistory as History } from '../../shared/api/guesthouses'

interface GuesthouseHistoryProps {
  history: History
}

export default function GuesthouseHistory({ history }: GuesthouseHistoryProps) {
  const { t } = useTranslation()

  return (
    <section
      className="detail-sheet detail-sheet--left history-sheet"
      aria-labelledby="history-heading"
    >
      <header className="detail-sheet-heading">
        <p className="section-index">06</p>
        <div>
          <p className="eyebrow">{t('guesthouses.history')}</p>
          <h2 id="history-heading">{history.title}</h2>
        </div>
      </header>
      <p className="history-text">{history.text}</p>
    </section>
  )
}
