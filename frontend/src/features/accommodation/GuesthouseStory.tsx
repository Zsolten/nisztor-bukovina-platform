import { useTranslation } from 'react-i18next'
import type { GuesthouseHistory } from '../../shared/api/guesthouses'

interface GuesthouseStoryProps {
  description: string
  roomDescription: string
  history: GuesthouseHistory
}

export default function GuesthouseStory({
  description,
  roomDescription,
  history,
}: GuesthouseStoryProps) {
  const { t } = useTranslation()

  return (
    <section className="detail-sheet detail-sheet--left story-sheet" aria-labelledby="story-heading">
      <div className="story-main">
        <p className="eyebrow">{t('guesthouses.history')}</p>
        <h2 id="story-heading">{history.title}</h2>
        <p className="story-lead">{description}</p>
        <p>{history.text}</p>
      </div>
      <aside className="room-note">
        <p className="eyebrow">{t('guesthouses.rooms')}</p>
        <p>{roomDescription}</p>
      </aside>
    </section>
  )
}
