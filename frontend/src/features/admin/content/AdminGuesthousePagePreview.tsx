import { useMemo, type ReactNode } from 'react'
import { Pencil } from 'lucide-react'
import { I18nextProvider } from 'react-i18next'
import GuesthouseDetailContent, {
  type GuesthouseContentSection,
} from '../../accommodation/GuesthouseDetailContent'
import { useGuesthouse } from '../../accommodation/useGuesthouseData'
import i18n from '../../../i18n/config'
import type { GuesthouseDetail } from '../../../shared/api/guesthouses'
import AsyncStatus from '../../../shared/components/AsyncStatus'
import type { AdminGuesthouseTranslation, ContentLanguage } from '../api/adminGuesthouseContent'

const SECTION_LABELS: Record<GuesthouseContentSection, string> = {
  hero: 'Nyitókép és cím',
  story: 'Bemutatkozás',
  rooms: 'Szobák',
  history: 'Történet és örökség',
}

interface AdminGuesthousePagePreviewProps {
  slug: string
  language: ContentLanguage
  draft: AdminGuesthouseTranslation
  selectedSection: GuesthouseContentSection
  onSelectSection: (section: GuesthouseContentSection) => void
}

export default function AdminGuesthousePagePreview({
  slug,
  language,
  draft,
  selectedSection,
  onSelectSection,
}: AdminGuesthousePagePreviewProps) {
  const { data, loading, error } = useGuesthouse(slug, language)
  const previewI18n = useMemo(
    () => i18n.cloneInstance({ initAsync: false, lng: language }),
    [language],
  )

  if (loading) {
    return <AsyncStatus variant="loading" message="Oldalelőnézet betöltése…" />
  }

  if (error || !data) {
    return (
      <AsyncStatus
        variant="error"
        message="Az oldalelőnézet nem tölthető be. A szövegek továbbra is szerkeszthetők."
      />
    )
  }

  const previewData: GuesthouseDetail = {
    ...data,
    name: draft.name,
    shortDescription: draft.shortDescription,
    description: draft.description,
    roomDescription: draft.roomDescription,
    history: {
      title: draft.historyTitle,
      text: draft.historyText,
    },
  }

  function wrapSection(section: GuesthouseContentSection, content: ReactNode) {
    const label = SECTION_LABELS[section]
    return (
      <div
        className={`admin-content-preview-region${selectedSection === section ? ' active' : ''}`}
        key={section}
      >
        {content}
        <button
          aria-label={`${label} szerkesztése`}
          onClick={() => onSelectSection(section)}
          type="button"
        >
          <Pencil aria-hidden="true" size={14} />
          <span>{label}</span>
        </button>
      </div>
    )
  }

  return (
    <I18nextProvider i18n={previewI18n}>
      <div className="admin-content-preview-page guesthouse-detail">
        <GuesthouseDetailContent
          data={previewData}
          language={language}
          showBackRow={false}
          wrapSection={wrapSection}
        />
      </div>
    </I18nextProvider>
  )
}
