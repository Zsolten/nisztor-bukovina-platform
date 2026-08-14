import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Badge, Button, Form, Modal, Spinner } from 'react-bootstrap'
import { ArrowDown, ArrowUp, Check, Pencil, Plus, Save, X } from 'lucide-react'
import {
  AdminAmenityApiError,
  type AdminAmenity,
  type AdminAmenityAssignment,
  type AdminAmenityTranslation,
  type AdminAmenityUpdate,
  type AmenityCategory,
  type AmenityLanguage,
  createAdminAmenity,
  fetchAdminAmenities,
  reorderAdminAmenities,
  updateAdminAmenity,
} from '../api/adminAmenities'
import { useAdminAuth } from '../auth/adminAuthContext'
import type { AdminGuesthouseContent, ContentLanguage } from '../api/adminGuesthouseContent'

interface AdminAmenityEditorProps {
  guesthouses: AdminGuesthouseContent[]
  selectedGuesthouseId: string
  language: ContentLanguage
  onCatalogueChange?: (amenities: AdminAmenity[]) => void
}

const CATEGORY_LABELS: Record<AmenityCategory, string> = {
  ROOM_COMFORT: 'Szobai kényelem',
  FOOD_KITCHEN: 'Étkezés és konyha',
  OUTDOOR_WELLNESS: 'Kültér és wellness',
  PROGRAM_GROUP: 'Program és közösség',
}

const EMPTY_TRANSLATIONS: AdminAmenityTranslation[] = ['hu', 'ro', 'en'].map((language) => ({
  language: language as AmenityLanguage,
  name: '',
  description: '',
  detailedDescription: '',
}))

const LANGUAGE_LABELS: Record<AmenityLanguage, string> = {
  hu: 'Magyar',
  ro: 'Román',
  en: 'Angol',
}

const IGNORE_CATALOGUE_CHANGE = () => undefined

export default function AdminAmenityEditor({
  guesthouses,
  selectedGuesthouseId,
  language,
  onCatalogueChange = IGNORE_CATALOGUE_CHANGE,
}: AdminAmenityEditorProps) {
  const { authorizedFetch } = useAdminAuth()
  const [amenities, setAmenities] = useState<AdminAmenity[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [feedback, setFeedback] = useState<{ variant: 'success' | 'danger'; message: string } | null>(
    null,
  )
  const [editing, setEditing] = useState<AdminAmenity | null>(null)
  const [editingError, setEditingError] = useState<string | null>(null)

  const load = useCallback(
    async (signal?: AbortSignal) => {
      setLoading(true)
      try {
        const result = await fetchAdminAmenities(authorizedFetch, signal)
        setAmenities(result)
        onCatalogueChange(result)
      } catch (error) {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setFeedback({ variant: 'danger', message: 'A szolgáltatások nem tölthetők be.' })
        }
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [authorizedFetch, onCatalogueChange],
  )

  useEffect(() => {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => void load(controller.signal), 0)
    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [load])

  const assignedAmenities = useMemo(
    () =>
      amenities
        .filter((amenity) => amenity.assignments.some((item) => item.guesthouseId === selectedGuesthouseId))
        .sort(
          (left, right) =>
            assignmentFor(left, selectedGuesthouseId)!.displayOrder -
            assignmentFor(right, selectedGuesthouseId)!.displayOrder,
        ),
    [amenities, selectedGuesthouseId],
  )

  function startCreate() {
    const currentMax = Math.max(
      -1,
      ...assignedAmenities.map((amenity) => assignmentFor(amenity, selectedGuesthouseId)!.displayOrder),
    )
    setFeedback(null)
    setEditingError(null)
    setEditing({
      id: '',
      code: '',
      category: 'ROOM_COMFORT',
      pricingType: 'FREE',
      translations: EMPTY_TRANSLATIONS.map((translation) => ({ ...translation })),
      assignments: [{ guesthouseId: selectedGuesthouseId, active: true, displayOrder: currentMax + 1 }],
    })
  }

  async function persist(draft: AdminAmenity, fromModal = false) {
    setSaving(true)
    setFeedback(null)
    if (fromModal) setEditingError(null)
    try {
      const payload: AdminAmenityUpdate = {
        code: draft.code.trim(),
        category: draft.category,
        pricingType: draft.pricingType,
        translations: draft.translations,
        assignments: draft.assignments,
      }
      const saved = draft.id
        ? await updateAdminAmenity(authorizedFetch, draft.id, payload)
        : await createAdminAmenity(authorizedFetch, payload)
      const index = amenities.findIndex((amenity) => amenity.id === saved.id)
      const nextAmenities =
        index < 0
          ? [...amenities, saved]
          : amenities.map((amenity) => (amenity.id === saved.id ? saved : amenity))
      setAmenities(nextAmenities)
      onCatalogueChange(nextAmenities)
      setEditing(null)
      setFeedback({ variant: 'success', message: 'A szolgáltatás mentése sikerült.' })
    } catch (error) {
      const message = amenityErrorMessage(error)
      if (fromModal) setEditingError(message)
      else setFeedback({ variant: 'danger', message })
    } finally {
      setSaving(false)
    }
  }

  async function toggleActive(amenity: AdminAmenity) {
    const assignment = assignmentFor(amenity, selectedGuesthouseId)
    if (!assignment) return
    await persist({
      ...amenity,
      assignments: amenity.assignments.map((item) =>
        item.guesthouseId === selectedGuesthouseId ? { ...item, active: !item.active } : item,
      ),
    })
  }

  async function move(amenityId: string, direction: -1 | 1) {
    const from = assignedAmenities.findIndex((amenity) => amenity.id === amenityId)
    const to = from + direction
    if (from < 0 || to < 0 || to >= assignedAmenities.length) return
    const orderedIds = assignedAmenities.map((amenity) => amenity.id)
    ;[orderedIds[from], orderedIds[to]] = [orderedIds[to], orderedIds[from]]
    setSaving(true)
    setFeedback(null)
    try {
      await reorderAdminAmenities(authorizedFetch, selectedGuesthouseId, orderedIds)
      const nextAmenities = amenities.map((amenity) => {
        const index = orderedIds.indexOf(amenity.id)
        if (index < 0) return amenity
        return {
          ...amenity,
          assignments: amenity.assignments.map((assignment) =>
            assignment.guesthouseId === selectedGuesthouseId
              ? { ...assignment, displayOrder: index }
              : assignment,
          ),
        }
      })
      setAmenities(nextAmenities)
      onCatalogueChange(nextAmenities)
    } catch (error) {
      setFeedback({ variant: 'danger', message: amenityErrorMessage(error) })
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="admin-amenity-status" role="status">
        <Spinner animation="border" size="sm" /> Szolgáltatások betöltése…
      </div>
    )
  }

  return (
    <section className="admin-amenity-editor" aria-labelledby="amenity-editor-heading">
      <div className="admin-amenity-heading">
        <div>
          <p className="admin-eyebrow">Szolgáltatáslista</p>
          <h3 id="amenity-editor-heading">Szolgáltatások és kikapcsolódás</h3>
          <p>A fordítás közös, de az aktív állapot és a sorrend panziónként állítható.</p>
        </div>
        <Button onClick={startCreate} size="sm">
          <Plus aria-hidden="true" size={16} /> Új szolgáltatás
        </Button>
      </div>

      {feedback && (
        <Alert className="admin-amenity-feedback" variant={feedback.variant}>
          {feedback.variant === 'success' && <Check aria-hidden="true" size={17} />}
          {feedback.message}
        </Alert>
      )}

      {assignedAmenities.length === 0 ? (
        <div className="admin-amenity-empty">Ehhez a panzióhoz még nincs szolgáltatás hozzárendelve.</div>
      ) : (
        <ul className="admin-amenity-list">
          {assignedAmenities.map((amenity, index) => {
            const translation = translationFor(amenity, language)
            const assignment = assignmentFor(amenity, selectedGuesthouseId)!
            return (
              <li className={!assignment.active ? 'inactive' : ''} key={amenity.id}>
                <div className="admin-amenity-order" aria-label={`${index + 1}. hely`}>
                  {index + 1}
                </div>
                <div className="admin-amenity-copy">
                  <strong>{translation.name || 'Névtelen fordítás'}</strong>
                  <span>{CATEGORY_LABELS[amenity.category]}</span>
                  <code>{amenity.code}</code>
                </div>
                <Badge bg={amenity.pricingType === 'FREE' ? 'success' : 'warning'} text="dark">
                  {amenity.pricingType === 'FREE' ? 'Ingyenes' : 'Fizetős'}
                </Badge>
                <Form.Check
                  aria-label={`${translation.name || amenity.code} aktív`}
                  checked={assignment.active}
                  disabled={saving}
                  label={assignment.active ? 'Aktív' : 'Inaktív'}
                  onChange={() => void toggleActive(amenity)}
                  role="switch"
                  type="switch"
                />
                <div className="admin-amenity-actions">
                  <Button
                    aria-label="Felébb"
                    disabled={saving || index === 0}
                    onClick={() => void move(amenity.id, -1)}
                    size="sm"
                    variant="outline-secondary"
                  >
                    <ArrowUp aria-hidden="true" size={16} />
                  </Button>
                  <Button
                    aria-label="Lejjebb"
                    disabled={saving || index === assignedAmenities.length - 1}
                    onClick={() => void move(amenity.id, 1)}
                    size="sm"
                    variant="outline-secondary"
                  >
                    <ArrowDown aria-hidden="true" size={16} />
                  </Button>
                  <Button
                    onClick={() => {
                      setEditingError(null)
                      setEditing(copyAmenity(amenity))
                    }}
                    size="sm"
                    variant="outline-primary"
                  >
                    <Pencil aria-hidden="true" size={16} /> Szerkesztés
                  </Button>
                </div>
              </li>
            )
          })}
        </ul>
      )}

      {editing && (
        <AmenityEditModal
          amenity={editing}
          error={editingError}
          guesthouses={guesthouses}
          initialLanguage={language}
          nextDisplayOrder={(guesthouseId) =>
            Math.max(
              -1,
              ...amenities
                .map((item) => assignmentFor(item, guesthouseId))
                .filter((item): item is AdminAmenityAssignment => Boolean(item))
                .map((item) => item.displayOrder),
            ) + 1
          }
          onChange={(amenity) => {
            setEditingError(null)
            setEditing(amenity)
          }}
          onHide={() => {
            setEditingError(null)
            setEditing(null)
          }}
          onSave={() => void persist(editing, true)}
          saving={saving}
        />
      )}
    </section>
  )
}

interface AmenityEditModalProps {
  amenity: AdminAmenity
  error: string | null
  guesthouses: AdminGuesthouseContent[]
  initialLanguage: ContentLanguage
  nextDisplayOrder: (guesthouseId: string) => number
  saving: boolean
  onChange: (amenity: AdminAmenity) => void
  onSave: () => void
  onHide: () => void
}

function AmenityEditModal({
  amenity,
  error,
  guesthouses,
  initialLanguage,
  nextDisplayOrder,
  saving,
  onChange,
  onSave,
  onHide,
}: AmenityEditModalProps) {
  const [language, setLanguage] = useState<AmenityLanguage>(initialLanguage)
  const translation = translationFor(amenity, language)
  const hungarian = translationFor(amenity, 'hu')
  const validCode = /^[a-z0-9]+(?:_[a-z0-9]+)*$/.test(amenity.code)

  function updateTranslation(field: keyof Omit<AdminAmenityTranslation, 'language'>, value: string) {
    const shouldUpdateCode =
      !amenity.id &&
      language === 'hu' &&
      field === 'name' &&
      (!amenity.code || amenity.code === toAmenityCode(translation.name))
    const nextAmenity = {
      ...amenity,
      translations: amenity.translations.map((item) =>
        item.language === language ? { ...item, [field]: value } : item,
      ),
    }
    onChange(
      shouldUpdateCode
        ? { ...nextAmenity, code: toAmenityCode(value) }
        : nextAmenity,
    )
  }

  function toggleAssignment(guesthouseId: string) {
    const existing = assignmentFor(amenity, guesthouseId)
    onChange({
      ...amenity,
      assignments: existing
        ? amenity.assignments.filter((item) => item.guesthouseId !== guesthouseId)
        : [
            ...amenity.assignments,
            {
              guesthouseId,
              active: true,
              displayOrder: nextDisplayOrder(guesthouseId),
            },
          ],
    })
  }

  return (
    <Modal centered className="admin-amenity-modal" onHide={onHide} scrollable show size="lg">
      <Modal.Header closeButton>
        <Modal.Title>{amenity.id ? 'Szolgáltatás szerkesztése' : 'Új szolgáltatás'}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form
          id="admin-amenity-form"
          onSubmit={(event) => {
            event.preventDefault()
            if (hungarian.name.trim() && validCode && !saving) onSave()
          }}
        >
          {error && <Alert variant="danger">{error}</Alert>}

          <div className="admin-amenity-language" aria-label="Szolgáltatás fordításának nyelve" role="tablist">
            {(Object.keys(LANGUAGE_LABELS) as AmenityLanguage[]).map((languageCode) => (
              <button
                aria-selected={language === languageCode}
                className={language === languageCode ? 'active' : ''}
                key={languageCode}
                onClick={() => setLanguage(languageCode)}
                role="tab"
                type="button"
              >
                {LANGUAGE_LABELS[languageCode]}
              </button>
            ))}
          </div>

          <div className="admin-amenity-form-grid">
            <Form.Group controlId="amenity-code">
              <Form.Label>Azonosító kód</Form.Label>
              <Form.Control
                isInvalid={Boolean(amenity.code) && !validCode}
                onChange={(event) => onChange({ ...amenity, code: event.target.value })}
                placeholder="peldaul: table_tennis"
                readOnly={Boolean(amenity.id)}
                value={amenity.code}
              />
              <Form.Text>
                {amenity.id
                  ? 'A kód létrehozás után nem módosítható.'
                  : 'A magyar névből automatikusan kitöltjük; csak kisbetű, szám és alsóvonás használható.'}
              </Form.Text>
              <Form.Control.Feedback type="invalid">
                Példa helyes kódra: table_tennis
              </Form.Control.Feedback>
            </Form.Group>
            <Form.Group controlId="amenity-category">
              <Form.Label>Kategória</Form.Label>
              <Form.Select
                onChange={(event) =>
                  onChange({ ...amenity, category: event.target.value as AmenityCategory })
                }
                value={amenity.category}
              >
                {Object.entries(CATEGORY_LABELS).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </div>

          <Form.Group className="admin-amenity-pricing" controlId="amenity-pricing">
            <Form.Label>Díjazás</Form.Label>
            <div>
              <Form.Check
                checked={amenity.pricingType === 'FREE'}
                id="amenity-free"
                label="Ingyenes szolgáltatás vagy eszköz"
                name="amenity-pricing"
                onChange={() => onChange({ ...amenity, pricingType: 'FREE' })}
                type="radio"
              />
              <Form.Check
                checked={amenity.pricingType === 'PAID'}
                id="amenity-paid"
                label="Fizetős szolgáltatás (az árazás később kezelhető)"
                name="amenity-pricing"
                onChange={() => onChange({ ...amenity, pricingType: 'PAID' })}
                type="radio"
              />
            </div>
          </Form.Group>

          <section className="admin-amenity-translation">
            <p className="admin-eyebrow">{LANGUAGE_LABELS[language]} fordítás</p>
            {language !== 'hu' && !translation.name && (
              <Alert variant="secondary">Üresen hagyva a publikus oldalon a magyar szöveg jelenik meg.</Alert>
            )}
            <Form.Group controlId="amenity-name">
              <Form.Label>Megnevezés {language === 'hu' && '*'}</Form.Label>
              <Form.Control
                isInvalid={language === 'hu' && !translation.name.trim()}
                onChange={(event) => updateTranslation('name', event.target.value)}
                value={translation.name}
              />
            </Form.Group>
            <Form.Group controlId="amenity-description">
              <Form.Label>Rövid leírás</Form.Label>
              <Form.Control
                as="textarea"
                onChange={(event) => updateTranslation('description', event.target.value)}
                rows={3}
                value={translation.description}
              />
            </Form.Group>
            <Form.Group controlId="amenity-detailed-description">
              <Form.Label>Részletes leírás (opcionális)</Form.Label>
              <Form.Control
                as="textarea"
                onChange={(event) => updateTranslation('detailedDescription', event.target.value)}
                rows={4}
                value={translation.detailedDescription}
              />
            </Form.Group>
          </section>

          <section className="admin-amenity-assignments">
            <p className="admin-eyebrow">Hozzárendelés</p>
            <p>Ugyanaz a szolgáltatás mindkét panziónál használható, külön aktiválással és sorrenddel.</p>
            {guesthouses.map((guesthouse) => {
              const name = guesthouse.translations.find((item) => item.language === 'hu')?.name ?? guesthouse.slug
              return (
                <Form.Check
                  checked={Boolean(assignmentFor(amenity, guesthouse.id))}
                  id={`amenity-assignment-${guesthouse.id}`}
                  key={guesthouse.id}
                  label={name}
                  onChange={() => toggleAssignment(guesthouse.id)}
                  type="checkbox"
                />
              )
            })}
          </section>
          {language !== 'hu' && !hungarian.name.trim() && (
            <Alert className="mt-3 mb-0" variant="warning">
              Előbb töltsd ki a magyar megnevezést, ez kötelező minden szolgáltatásnál.
            </Alert>
          )}
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button disabled={saving} onClick={onHide} variant="outline-secondary">
          <X aria-hidden="true" size={16} /> Mégse
        </Button>
        <Button
          disabled={saving || !hungarian.name.trim() || !validCode}
          form="admin-amenity-form"
          type="submit"
        >
          {saving ? <Spinner animation="border" size="sm" /> : <Save aria-hidden="true" size={16} />}
          {saving ? 'Mentés…' : 'Szolgáltatás mentése'}
        </Button>
      </Modal.Footer>
    </Modal>
  )
}

function assignmentFor(amenity: AdminAmenity, guesthouseId: string) {
  return amenity.assignments.find((assignment) => assignment.guesthouseId === guesthouseId)
}

function translationFor(amenity: AdminAmenity, language: AmenityLanguage) {
  return (
    amenity.translations.find((translation) => translation.language === language) ?? {
      language,
      name: '',
      description: '',
      detailedDescription: '',
    }
  )
}

function copyAmenity(amenity: AdminAmenity): AdminAmenity {
  return {
    ...amenity,
    translations: amenity.translations.map((translation) => ({ ...translation })),
    assignments: amenity.assignments.map((assignment) => ({ ...assignment })),
  }
}

function amenityErrorMessage(error: unknown) {
  if (error instanceof AdminAmenityApiError) {
    if (error.code === 'AMENITY_CODE_ALREADY_EXISTS') return 'Ez az azonosító kód már foglalt.'
    if (error.code === 'INVALID_AMENITY_ORDER') return 'A szolgáltatások sorrendje időközben megváltozott. Töltsd be újra az oldalt.'
    if (error.code === 'ADMIN_AMENITY_VALIDATION_FAILED') {
      return 'Ellenőrizd a megadott szolgáltatásadatokat és a hozzárendelést.'
    }
  }
  return 'A szolgáltatás mentése nem sikerült. Próbáld újra.'
}

function toAmenityCode(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
}
