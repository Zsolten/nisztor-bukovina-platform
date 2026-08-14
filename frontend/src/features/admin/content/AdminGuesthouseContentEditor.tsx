import { useCallback, useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { Alert, Badge, Button, Form, Modal, Spinner } from 'react-bootstrap'
import {
  AlertTriangle,
  BedDouble,
  BookOpen,
  Check,
  CircleDollarSign,
  Eye,
  House,
  Images,
  RefreshCw,
  Save,
  Sparkles,
  Type,
  Utensils,
} from 'lucide-react'
import { useBeforeUnload, useBlocker } from 'react-router-dom'
import type { GuesthouseContentSection } from '../../accommodation/GuesthouseDetailContent'
import {
  AdminGuesthouseContentApiError,
  fetchAdminGuesthouseContent,
  updateAdminGuesthouseTranslation,
  type AdminGuesthouseContent,
  type AdminGuesthouseTranslation,
  type ContentField,
  type ContentLanguage,
} from '../api/adminGuesthouseContent'
import { useAdminAuth } from '../auth/adminAuthContext'
import AdminAmenityEditor from './AdminAmenityEditor'
import AdminGuesthousePagePreview from './AdminGuesthousePagePreview'

const LANGUAGES: Array<{ code: ContentLanguage; label: string }> = [
  { code: 'hu', label: 'Magyar' },
  { code: 'ro', label: 'Román' },
  { code: 'en', label: 'Angol' },
]

const FIELD_LIMITS: Record<ContentField, number> = {
  name: 160,
  shortDescription: 500,
  description: 5000,
  roomDescription: 3000,
  storyEyebrow: 240,
  storyTitle: 240,
  diningEyebrow: 240,
  diningTitle: 240,
  diningDescription: 1000,
  amenitiesTitle: 240,
  roomTypesTitle: 240,
  pricingTitle: 240,
  historyEyebrow: 240,
  historyTitle: 240,
  historyText: 5000,
  galleryTitle: 240,
  galleryHint: 500,
}

const SECTION_FIELDS: Record<GuesthouseContentSection, ContentField[]> = {
  hero: ['name', 'shortDescription'],
  story: ['storyEyebrow', 'storyTitle', 'description'],
  dining: ['diningEyebrow', 'diningTitle', 'diningDescription'],
  amenities: ['amenitiesTitle'],
  rooms: ['roomTypesTitle', 'roomDescription'],
  pricing: ['pricingTitle'],
  history: ['historyEyebrow', 'historyTitle', 'historyText'],
  gallery: ['galleryTitle', 'galleryHint'],
}

const SECTION_DETAILS: Array<{
  id: GuesthouseContentSection
  label: string
  description: string
  icon: typeof House
}> = [
  {
    id: 'hero',
    label: 'Nyitókép és cím',
    description: 'A panzió neve és rövid bemutatása.',
    icon: House,
  },
  {
    id: 'story',
    label: 'Bemutatkozás',
    description: 'A panzió részletes bemutatkozó szövege.',
    icon: Type,
  },
  {
    id: 'dining',
    label: 'Étkezés',
    description: 'Az étkezési szakasz címei és bemutatkozó szövege.',
    icon: Utensils,
  },
  {
    id: 'amenities',
    label: 'Szolgáltatások',
    description: 'A szolgáltatások szakasz címe.',
    icon: Sparkles,
  },
  {
    id: 'rooms',
    label: 'Szobák',
    description: 'A szobatípusok előtt megjelenő bevezető.',
    icon: BedDouble,
  },
  {
    id: 'pricing',
    label: 'Árak',
    description: 'Az árakat és feltételeket bemutató szakasz címe.',
    icon: CircleDollarSign,
  },
  {
    id: 'history',
    label: 'Történet és örökség',
    description: 'A panzióoldal történeti zárószakasza.',
    icon: BookOpen,
  },
  {
    id: 'gallery',
    label: 'Képgaléria',
    description: 'A galéria címe és rövid segédszövege.',
    icon: Images,
  },
]

const FIELD_SECTION = Object.fromEntries(
  Object.entries(SECTION_FIELDS).flatMap(([section, fields]) =>
    fields.map((field) => [field, section]),
  ),
) as Record<ContentField, GuesthouseContentSection>

const FIELD_DETAILS: Record<ContentField, { label: string; multiline?: boolean; large?: boolean }> =
  {
    name: { label: 'Panzió neve' },
    shortDescription: { label: 'Rövid leírás', multiline: true },
    description: { label: 'Részletes leírás', multiline: true, large: true },
    roomDescription: { label: 'Szobák bevezető szövege', multiline: true },
    storyEyebrow: { label: 'Bemutatkozás labelje' },
    storyTitle: { label: 'Bemutatkozás címe' },
    diningEyebrow: { label: 'Étkezés labelje' },
    diningTitle: { label: 'Étkezés címe' },
    diningDescription: { label: 'Étkezés leírása', multiline: true },
    amenitiesTitle: { label: 'Szolgáltatások címe' },
    roomTypesTitle: { label: 'Szobatípusok címe' },
    pricingTitle: { label: 'Árak címe' },
    historyEyebrow: { label: 'Történeti rész labelje' },
    historyTitle: { label: 'Történet címe' },
    historyText: { label: 'Történet szövege', multiline: true, large: true },
    galleryTitle: { label: 'Galéria címe' },
    galleryHint: { label: 'Galéria segédszövege', multiline: true },
  }

type TranslationMap = Record<string, AdminGuesthouseTranslation>
type FieldErrors = Partial<Record<ContentField, string>>

function translationKey(guesthouseId: string, language: ContentLanguage) {
  return `${guesthouseId}:${language}`
}

function toTranslationMap(guesthouses: AdminGuesthouseContent[]) {
  return Object.fromEntries(
    guesthouses.flatMap((guesthouse) =>
      guesthouse.translations.map((translation) => [
        translationKey(guesthouse.id, translation.language),
        translation,
      ]),
    ),
  ) as TranslationMap
}

function validate(translation: AdminGuesthouseTranslation) {
  const errors: FieldErrors = {}
  ;(Object.keys(FIELD_LIMITS) as ContentField[]).forEach((field) => {
    const value = translation[field]
    if (!value.trim()) errors[field] = 'A mező kitöltése kötelező.'
    else if (value.length > FIELD_LIMITS[field])
      errors[field] = `Legfeljebb ${FIELD_LIMITS[field]} karakter adható meg.`
  })
  return errors
}

export default function AdminGuesthouseContentEditor() {
  const { authorizedFetch } = useAdminAuth()
  const [guesthouses, setGuesthouses] = useState<AdminGuesthouseContent[]>([])
  const [saved, setSaved] = useState<TranslationMap>({})
  const [drafts, setDrafts] = useState<TranslationMap>({})
  const [guesthouseId, setGuesthouseId] = useState('')
  const [language, setLanguage] = useState<ContentLanguage>('hu')
  const [selectedSection, setSelectedSection] = useState<GuesthouseContentSection>('hero')
  const [mobileView, setMobileView] = useState<'preview' | 'edit'>('edit')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [feedback, setFeedback] = useState<{
    variant: 'success' | 'danger' | 'warning'
    message: string
  } | null>(null)
  const [conflict, setConflict] = useState<AdminGuesthouseTranslation | null>(null)
  const [dismissedBlockLocationKey, setDismissedBlockLocationKey] = useState<string | null>(null)

  const currentKey = guesthouseId ? translationKey(guesthouseId, language) : ''
  const draft = drafts[currentKey]
  const selectedGuesthouse = guesthouses.find((item) => item.id === guesthouseId)
  const selectedSectionDetails = SECTION_DETAILS.find((item) => item.id === selectedSection)!
  const dirty = useMemo(
    () =>
      Object.keys(drafts).some((key) => JSON.stringify(drafts[key]) !== JSON.stringify(saved[key])),
    [drafts, saved],
  )

  const load = useCallback(
    async (signal?: AbortSignal) => {
      setLoading(true)
      setFeedback(null)
      try {
        const result = await fetchAdminGuesthouseContent(authorizedFetch, signal)
        const translations = toTranslationMap(result)
        setGuesthouses(result)
        setSaved(translations)
        setDrafts(translations)
        setGuesthouseId((current) => current || result[0]?.id || '')
      } catch (error) {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setFeedback({
            variant: 'danger',
            message: 'A panziótartalmak nem tölthetők be. Próbálja újra.',
          })
        }
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [authorizedFetch],
  )

  useEffect(() => {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => void load(controller.signal), 0)
    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [load])

  useBeforeUnload(
    useCallback(
      (event) => {
        if (dirty) event.preventDefault()
      },
      [dirty],
    ),
  )
  const blocker = useBlocker(dirty)
  const blockedLocationKey = blocker.state === 'blocked' ? blocker.location.key : null

  function stayOnPage() {
    setDismissedBlockLocationKey(blockedLocationKey)
    blocker.reset?.()
  }

  function leaveWithoutSaving() {
    setDismissedBlockLocationKey(blockedLocationKey)
    blocker.proceed?.()
  }

  function updateField(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const field = event.target.name as ContentField
    const value = event.target.value
    setDrafts((current) => ({
      ...current,
      [currentKey]: { ...current[currentKey], [field]: value },
    }))
    setFieldErrors((current) => ({ ...current, [field]: undefined }))
    setFeedback(null)
    setConflict(null)
  }

  async function saveCurrent(overwriteVersion?: number | null) {
    if (!draft || !guesthouseId) return
    const content = overwriteVersion === undefined ? draft : { ...draft, version: overwriteVersion }
    const errors = validate(content)
    setFieldErrors(errors)
    if (Object.keys(errors).length) {
      const firstInvalidField = Object.keys(errors)[0] as ContentField
      setSelectedSection(FIELD_SECTION[firstInvalidField])
      setMobileView('edit')
      setFeedback({ variant: 'danger', message: 'Ellenőrizze a megjelölt mezőket.' })
      return
    }

    setSaving(true)
    setFeedback(null)
    try {
      const result = await updateAdminGuesthouseTranslation(authorizedFetch, guesthouseId, content)
      setDrafts((current) => ({ ...current, [currentKey]: result }))
      setSaved((current) => ({ ...current, [currentKey]: result }))
      setConflict(null)
      setFeedback({ variant: 'success', message: 'A tartalom mentése sikerült.' })
    } catch (error) {
      if (error instanceof AdminGuesthouseContentApiError) {
        if (error.code === 'ADMIN_CONTENT_VERSION_CONFLICT') {
          setConflict(error.currentContent)
          setFeedback({
            variant: 'warning',
            message: 'Ezt a fordítást időközben más is módosította.',
          })
        } else if (error.code === 'ADMIN_CONTENT_VALIDATION_FAILED') {
          setFieldErrors(
            Object.fromEntries(
              Object.entries(error.fieldErrors).map(([field, code]) => [
                field,
                code === 'REQUIRED'
                  ? 'A mező kitöltése kötelező.'
                  : `Legfeljebb ${FIELD_LIMITS[field as ContentField]} karakter adható meg.`,
              ]),
            ),
          )
          setFeedback({ variant: 'danger', message: 'A szerver elutasította a megjelölt mezőket.' })
        } else {
          setFeedback({ variant: 'danger', message: errorMessage(error.code) })
        }
      } else {
        setFeedback({ variant: 'danger', message: 'A mentés nem sikerült. Próbálja újra.' })
      }
    } finally {
      setSaving(false)
    }
  }

  function reloadConflict() {
    if (!conflict) {
      void load()
      return
    }
    setDrafts((current) => ({ ...current, [currentKey]: conflict }))
    setSaved((current) => ({ ...current, [currentKey]: conflict }))
    setConflict(null)
    setFeedback(null)
  }

  if (loading) {
    return (
      <div className="admin-content-status" role="status">
        <Spinner animation="border" size="sm" /> Tartalmak betöltése…
      </div>
    )
  }

  if (!draft || !selectedGuesthouse) {
    return (
      <Alert className="admin-content-status" variant="danger">
        {feedback?.message ?? 'Nincs szerkeszthető panziótartalom.'}
        <Button className="ms-3" onClick={() => void load()} size="sm" variant="outline-danger">
          Újrapróbálás
        </Button>
      </Alert>
    )
  }

  return (
    <section className="admin-content-editor">
      <header className="admin-content-heading">
        <div>
          <p className="admin-eyebrow">Publikus tartalom</p>
          <h1>Panziók bemutatkozása</h1>
          <p>A mentett szöveg azonnal megjelenik a publikus oldalon.</p>
        </div>
        {dirty && (
          <Badge bg="warning" text="dark">
            Nem mentett módosítás
          </Badge>
        )}
      </header>

      <div className="admin-content-toolbar">
        <Form.Group controlId="content-guesthouse">
          <Form.Label>Panzió</Form.Label>
          <Form.Select
            value={guesthouseId}
            onChange={(event) => {
              setGuesthouseId(event.target.value)
              setFieldErrors({})
              setFeedback(null)
              setConflict(null)
            }}
          >
            {guesthouses.map((guesthouse) => {
              const hungarian = drafts[translationKey(guesthouse.id, 'hu')]
              return (
                <option key={guesthouse.id} value={guesthouse.id}>
                  {hungarian?.name || guesthouse.slug}
                  {guesthouse.active ? '' : ' (inaktív)'}
                </option>
              )
            })}
          </Form.Select>
        </Form.Group>

        <div className="admin-content-language" aria-label="Szerkesztés nyelve" role="tablist">
          {LANGUAGES.map((item) => (
            <button
              aria-selected={language === item.code}
              className={language === item.code ? 'active' : ''}
              key={item.code}
              onClick={() => {
                setLanguage(item.code)
                setFieldErrors({})
                setFeedback(null)
                setConflict(null)
              }}
              role="tab"
              type="button"
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {feedback && (
        <Alert className="admin-content-feedback" variant={feedback.variant}>
          {feedback.variant === 'success' && <Check aria-hidden="true" size={18} />}
          <span>{feedback.message}</span>
          {feedback.variant === 'danger' && !Object.keys(fieldErrors).length && (
            <Button onClick={() => void saveCurrent()} size="sm" variant="outline-danger">
              Újrapróbálás
            </Button>
          )}
        </Alert>
      )}

      {feedback?.variant === 'warning' && (
        <div className="admin-content-conflict">
          <Button onClick={reloadConflict} variant="outline-secondary">
            <RefreshCw aria-hidden="true" size={16} /> Szerverváltozat betöltése
          </Button>
          {conflict && (
            <Button onClick={() => void saveCurrent(conflict.version)} variant="warning">
              Saját változat felülírása
            </Button>
          )}
        </div>
      )}

      <div className="admin-content-mobile-view" aria-label="Tartalomnézet">
        <button
          className={mobileView === 'preview' ? 'active' : ''}
          onClick={() => setMobileView('preview')}
          type="button"
        >
          <Eye aria-hidden="true" size={17} /> Előnézet
        </button>
        <button
          className={mobileView === 'edit' ? 'active' : ''}
          onClick={() => setMobileView('edit')}
          type="button"
        >
          <Type aria-hidden="true" size={17} /> Szerkesztés
        </button>
      </div>

      <div className={`admin-content-workspace mobile-${mobileView} section-${selectedSection}`}>
        <section className="admin-content-preview" aria-labelledby="content-preview-heading">
          <header>
            <div>
              <p className="admin-eyebrow">Élő előnézet</p>
              <h2 id="content-preview-heading">A publikus panzióoldal</h2>
            </div>
            <span>A keretezett részek szerkeszthetők</span>
          </header>
          <div className="admin-content-preview-viewport">
            <AdminGuesthousePagePreview
              draft={draft}
              language={language}
              onSelectSection={(section) => {
                setSelectedSection(section)
                setMobileView('edit')
              }}
              selectedSection={selectedSection}
              slug={selectedGuesthouse.slug}
            />
          </div>
        </section>

        <aside className="admin-content-inspector" aria-labelledby="content-inspector-heading">
          <nav aria-label="Oldalszakaszok" className="admin-content-section-nav">
            {SECTION_DETAILS.map((section) => {
              const Icon = section.icon
              return (
                <button
                  aria-current={selectedSection === section.id ? 'true' : undefined}
                  className={selectedSection === section.id ? 'active' : ''}
                  key={section.id}
                  onClick={() => setSelectedSection(section.id)}
                  type="button"
                >
                  <Icon aria-hidden="true" size={17} />
                  <span>{section.label}</span>
                </button>
              )
            })}
          </nav>

          <Form
            className="admin-content-form"
            onSubmit={(event) => {
              event.preventDefault()
              void saveCurrent()
            }}
          >
            <header className="admin-content-form-heading">
              <p className="admin-eyebrow">Kiválasztott szakasz</p>
              <h2 id="content-inspector-heading">{selectedSectionDetails.label}</h2>
              <p>{selectedSectionDetails.description}</p>
            </header>

            {SECTION_FIELDS[selectedSection].map((field) => {
              const details = FIELD_DETAILS[field]
              return (
                <ContentFieldInput
                  error={fieldErrors[field]}
                  field={field}
                  key={field}
                  label={details.label}
                  large={details.large}
                  multiline={details.multiline}
                  onChange={updateField}
                  value={draft[field]}
                />
              )
            })}

            <div className="admin-content-save">
              <span>{selectedGuesthouse.active ? 'Aktív panzió' : 'Inaktív panzió'}</span>
              <Button
                disabled={saving || JSON.stringify(draft) === JSON.stringify(saved[currentKey])}
                type="submit"
              >
                {saving ? (
                  <Spinner animation="border" size="sm" />
                ) : (
                  <Save aria-hidden="true" size={17} />
                )}
                {saving ? 'Mentés…' : 'Fordítás mentése'}
              </Button>
            </div>
          </Form>

        </aside>
      </div>

      {selectedSection === 'amenities' && (
        <AdminAmenityEditor
          guesthouses={guesthouses}
          language={language}
          selectedGuesthouseId={guesthouseId}
        />
      )}

      <Modal
        centered
        className="admin-unsaved-changes-modal"
        contentClassName="admin-unsaved-changes-modal-content"
        onHide={stayOnPage}
        show={blocker.state === 'blocked' && dismissedBlockLocationKey !== blockedLocationKey}
      >
        <Modal.Body>
          <span className="admin-unsaved-changes-icon" aria-hidden="true">
            <AlertTriangle size={22} />
          </span>
          <p className="admin-eyebrow">Mentetlen szerkesztés</p>
          <h2>Elveti a módosításokat?</h2>
          <p>A kiválasztott panzió fordításán végzett változtatások nem lettek mentve.</p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="outline-secondary" onClick={stayOnPage}>
            Maradok az oldalon
          </Button>
          <Button className="admin-unsaved-changes-leave" onClick={leaveWithoutSaving}>
            Kilépés mentés nélkül
          </Button>
        </Modal.Footer>
      </Modal>
    </section>
  )
}

interface ContentFieldInputProps {
  field: ContentField
  label: string
  value: string
  error?: string
  multiline?: boolean
  large?: boolean
  onChange: (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void
}

function ContentFieldInput({
  field,
  label,
  value,
  error,
  multiline,
  large,
  onChange,
}: ContentFieldInputProps) {
  const controlId = `content-${field}`
  return (
    <Form.Group
      className={large ? 'admin-content-field admin-content-field--large' : 'admin-content-field'}
      controlId={controlId}
    >
      <div className="admin-content-field-heading">
        <Form.Label>{label}</Form.Label>
        <span className={value.length > FIELD_LIMITS[field] ? 'over-limit' : ''}>
          {value.length} / {FIELD_LIMITS[field]}
        </span>
      </div>
      {multiline ? (
        <Form.Control
          as="textarea"
          isInvalid={Boolean(error)}
          maxLength={FIELD_LIMITS[field] + 100}
          name={field}
          onChange={onChange}
          rows={large ? 7 : 4}
          value={value}
        />
      ) : (
        <Form.Control
          isInvalid={Boolean(error)}
          maxLength={FIELD_LIMITS[field] + 100}
          name={field}
          onChange={onChange}
          value={value}
        />
      )}
      <Form.Control.Feedback type="invalid">{error}</Form.Control.Feedback>
    </Form.Group>
  )
}

function errorMessage(code?: string) {
  if (code === 'ADMIN_GUESTHOUSE_NOT_FOUND') return 'A kiválasztott panzió már nem található.'
  if (code === 'UNSUPPORTED_CONTENT_LANGUAGE') return 'Ez a nyelv nem támogatott.'
  if (code === 'INVALID_ADMIN_CONTENT_REQUEST') return 'A küldött tartalom nem értelmezhető.'
  return 'A mentés nem sikerült. Próbálja újra.'
}
