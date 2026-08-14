import type { GuesthouseHistory as History } from '../../shared/api/guesthouses'

interface GuesthouseHistoryProps {
  history: History
  eyebrow: string
}

export default function GuesthouseHistory({ history, eyebrow }: GuesthouseHistoryProps) {
  return (
    <section
      className="detail-sheet detail-sheet--left history-sheet"
      aria-labelledby="history-heading"
    >
      <header className="detail-sheet-heading">
        <p className="section-index">06</p>
        <div>
          <p className="eyebrow">{eyebrow}</p>
          <h2 id="history-heading">{history.title}</h2>
        </div>
      </header>
      <p className="history-text">{history.text}</p>
    </section>
  )
}
