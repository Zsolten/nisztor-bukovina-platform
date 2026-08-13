import { useCallback, useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { Alert, Badge, Button, Form, Spinner } from 'react-bootstrap'
import { Check, RefreshCw, Save } from 'lucide-react'
import { useBeforeUnload, useBlocker } from 'react-router-dom'
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
  historyTitle: 240,
  historyText: 5000,
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
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [feedback, setFeedback] = useState<{
    variant: 'success' | 'danger' | 'warning'
    message: string
  } | null>(null)
  const [conflict, setConflict] = useState<AdminGuesthouseTranslation | null>(null)

  const currentKey = guesthouseId ? translationKey(guesthouseId, language) : ''
  const draft = drafts[currentKey]
  const selectedGuesthouse = guesthouses.find((item) => item.id === guesthouseId)
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
  useEffect(() => {
    if (blocker.state !== 'blocked') return
    if (window.confirm('Nem mentett módosításai vannak. Biztosan elhagyja az oldalt?')) {
      blocker.proceed()
    } else {
      blocker.reset()
    }
  }, [blocker])

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

      <Form
        className="admin-content-form"
        onSubmit={(event) => {
          event.preventDefault()
          void saveCurrent()
        }}
      >
        <ContentFieldInput
          field="name"
          label="Panzió neve"
          value={draft.name}
          error={fieldErrors.name}
          onChange={updateField}
        />
        <ContentFieldInput
          field="shortDescription"
          label="Rövid leírás"
          value={draft.shortDescription}
          error={fieldErrors.shortDescription}
          onChange={updateField}
          multiline
        />
        <ContentFieldInput
          field="description"
          label="Részletes leírás"
          value={draft.description}
          error={fieldErrors.description}
          onChange={updateField}
          multiline
          large
        />
        <ContentFieldInput
          field="roomDescription"
          label="Szobák bevezető szövege"
          value={draft.roomDescription}
          error={fieldErrors.roomDescription}
          onChange={updateField}
          multiline
        />
        <ContentFieldInput
          field="historyTitle"
          label="Történet címe"
          value={draft.historyTitle}
          error={fieldErrors.historyTitle}
          onChange={updateField}
        />
        <ContentFieldInput
          field="historyText"
          label="Történet szövege"
          value={draft.historyText}
          error={fieldErrors.historyText}
          onChange={updateField}
          multiline
          large
        />

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
