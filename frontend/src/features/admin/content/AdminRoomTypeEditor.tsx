import { useCallback, useEffect, useState } from 'react'
import { Alert, Badge, Button, Form, Modal, Spinner } from 'react-bootstrap'
import { ArrowDown, ArrowUp, Check, Pencil, Plus, Save, X } from 'lucide-react'
import {
  AdminRoomTypeApiError,
  createAdminRoomType,
  fetchAdminRoomTypes,
  reorderAdminRoomTypes,
  type AdminRoomType,
  type AdminRoomTypeTranslation,
  type AdminRoomTypeUpdate,
  type RoomTypeLanguage,
  updateAdminRoomType,
} from '../api/adminRoomTypes'
import { useAdminAuth } from '../auth/adminAuthContext'
import type { ContentLanguage } from '../api/adminGuesthouseContent'

interface AdminRoomTypeEditorProps {
  guesthouseId: string
  guesthouseName: string
  language: ContentLanguage
  onSaved?: () => void
}

const LANGUAGE_LABELS: Record<RoomTypeLanguage, string> = {
  hu: 'Magyar',
  ro: 'Román',
  en: 'Angol',
}

const EMPTY_TRANSLATIONS: AdminRoomTypeTranslation[] = ['hu', 'ro', 'en'].map((language) => ({
  language: language as RoomTypeLanguage,
  name: '',
  shortDescription: '',
  detailedDescription: '',
}))

export default function AdminRoomTypeEditor({
  guesthouseId,
  guesthouseName,
  language,
  onSaved,
}: AdminRoomTypeEditorProps) {
  const { authorizedFetch } = useAdminAuth()
  const [roomTypes, setRoomTypes] = useState<AdminRoomType[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [feedback, setFeedback] = useState<{
    variant: 'success' | 'danger'
    message: string
  } | null>(null)
  const [editing, setEditing] = useState<AdminRoomType | null>(null)

  const load = useCallback(
    async (signal?: AbortSignal) => {
      setLoading(true)
      try {
        setRoomTypes(await fetchAdminRoomTypes(authorizedFetch, guesthouseId, signal))
      } catch (error) {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setFeedback({ variant: 'danger', message: 'A szobatípusok nem tölthetők be.' })
        }
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [authorizedFetch, guesthouseId],
  )

  useEffect(() => {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => void load(controller.signal), 0)
    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [load])

  function startCreate() {
    setFeedback(null)
    setEditing({
      id: '',
      code: '',
      quantity: 1,
      standardOccupancy: 2,
      roomsWithExtraBed: 0,
      extraBedsPerEligibleRoom: 0,
      active: true,
      displayOrder: roomTypes.length,
      translations: EMPTY_TRANSLATIONS.map((translation) => ({ ...translation })),
    })
  }

  async function save(draft: AdminRoomType) {
    setSaving(true)
    setFeedback(null)
    try {
      const payload: AdminRoomTypeUpdate = {
        code: draft.code.trim(),
        quantity: draft.quantity,
        standardOccupancy: draft.standardOccupancy,
        roomsWithExtraBed: draft.roomsWithExtraBed,
        extraBedsPerEligibleRoom: draft.extraBedsPerEligibleRoom,
        active: draft.active,
        translations: draft.translations,
      }
      const saved = draft.id
        ? await updateAdminRoomType(authorizedFetch, guesthouseId, draft.id, payload)
        : await createAdminRoomType(authorizedFetch, guesthouseId, payload)
      setRoomTypes((current) => {
        const index = current.findIndex((roomType) => roomType.id === saved.id)
        return index < 0
          ? [...current, saved]
          : current.map((item) => (item.id === saved.id ? saved : item))
      })
      setEditing(null)
      setFeedback({ variant: 'success', message: 'A szobatípus mentése sikerült.' })
      onSaved?.()
    } catch (error) {
      setFeedback({ variant: 'danger', message: roomTypeErrorMessage(error) })
    } finally {
      setSaving(false)
    }
  }

  async function toggleActive(roomType: AdminRoomType) {
    if (
      roomType.active &&
      !window.confirm(
        `Biztosan inaktiválod ezt a szobatípust: ${translationFor(roomType, 'hu').name}?`,
      )
    )
      return
    await save({ ...roomType, active: !roomType.active })
  }

  async function move(index: number, direction: -1 | 1) {
    const target = index + direction
    if (target < 0 || target >= roomTypes.length) return
    const next = [...roomTypes]
    ;[next[index], next[target]] = [next[target], next[index]]
    setSaving(true)
    try {
      await reorderAdminRoomTypes(
        authorizedFetch,
        guesthouseId,
        next.map((roomType) => roomType.id),
      )
      setRoomTypes(next.map((roomType, displayOrder) => ({ ...roomType, displayOrder })))
      onSaved?.()
    } catch (error) {
      setFeedback({ variant: 'danger', message: roomTypeErrorMessage(error) })
    } finally {
      setSaving(false)
    }
  }

  if (loading)
    return (
      <div className="admin-amenity-status" role="status">
        <Spinner animation="border" size="sm" /> Szobatípusok betöltése…
      </div>
    )

  return (
    <section className="admin-amenity-editor" aria-labelledby="room-type-editor-heading">
      <div className="admin-amenity-heading">
        <div>
          <p className="admin-eyebrow">Szobatípusok</p>
          <h3 id="room-type-editor-heading">{guesthouseName} szobái</h3>
          <p>A darabszám határozza meg, hogy a foglaló legfeljebb hány ilyen szobát választhat.</p>
        </div>
        <Button onClick={startCreate} size="sm">
          <Plus aria-hidden="true" size={16} /> Új szobatípus
        </Button>
      </div>

      {feedback && (
        <Alert className="admin-amenity-feedback" variant={feedback.variant}>
          {feedback.variant === 'success' && <Check aria-hidden="true" size={17} />}
          {feedback.message}
        </Alert>
      )}

      {roomTypes.length === 0 ? (
        <div className="admin-amenity-empty">Ehhez a panzióhoz még nincs szobatípus.</div>
      ) : (
        <ul className="admin-amenity-list admin-room-type-list">
          {roomTypes.map((roomType, index) => {
            const translation = translationFor(roomType, language)
            return (
              <li className={!roomType.active ? 'inactive' : ''} key={roomType.id}>
                <div className="admin-amenity-order">{index + 1}</div>
                <div className="admin-amenity-copy">
                  <strong>{translation.name || translationFor(roomType, 'hu').name}</strong>
                  <span>
                    {roomType.quantity} db · {roomType.standardOccupancy} ágyas
                  </span>
                </div>
                <Badge bg="secondary">max. {roomType.quantity} választható</Badge>
                <Form.Check
                  aria-label={`${translation.name || roomType.code} aktív`}
                  checked={roomType.active}
                  disabled={saving}
                  label={roomType.active ? 'Aktív' : 'Inaktív'}
                  onChange={() => void toggleActive(roomType)}
                  role="switch"
                  type="switch"
                />
                <div className="admin-amenity-actions">
                  <Button
                    aria-label="Felébb"
                    disabled={saving || index === 0}
                    onClick={() => void move(index, -1)}
                    size="sm"
                    variant="outline-secondary"
                  >
                    <ArrowUp aria-hidden="true" size={16} />
                  </Button>
                  <Button
                    aria-label="Lejjebb"
                    disabled={saving || index === roomTypes.length - 1}
                    onClick={() => void move(index, 1)}
                    size="sm"
                    variant="outline-secondary"
                  >
                    <ArrowDown aria-hidden="true" size={16} />
                  </Button>
                  <Button
                    onClick={() => setEditing(copyRoomType(roomType))}
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
        <RoomTypeEditModal
          initialLanguage={language}
          roomType={editing}
          saving={saving}
          onChange={setEditing}
          onHide={() => setEditing(null)}
          onSave={() => void save(editing)}
        />
      )}
    </section>
  )
}

function RoomTypeEditModal({
  initialLanguage,
  roomType,
  saving,
  onChange,
  onHide,
  onSave,
}: {
  initialLanguage: ContentLanguage
  roomType: AdminRoomType
  saving: boolean
  onChange: (roomType: AdminRoomType) => void
  onHide: () => void
  onSave: () => void
}) {
  const [language, setLanguage] = useState<RoomTypeLanguage>(initialLanguage)
  const translation = translationFor(roomType, language)
  const hungarian = translationFor(roomType, 'hu')
  const validCode = /^[a-z0-9]+(?:_[a-z0-9]+)*$/.test(roomType.code)
  const validExtraBeds =
    roomType.roomsWithExtraBed <= roomType.quantity &&
    (roomType.roomsWithExtraBed === 0
      ? roomType.extraBedsPerEligibleRoom === 0
      : roomType.extraBedsPerEligibleRoom > 0)

  function changeTranslation(
    field: keyof Omit<AdminRoomTypeTranslation, 'language'>,
    value: string,
  ) {
    const autoCode =
      !roomType.id &&
      language === 'hu' &&
      field === 'name' &&
      (!roomType.code || roomType.code === toRoomTypeCode(translation.name))
    const next = {
      ...roomType,
      translations: roomType.translations.map((item) =>
        item.language === language ? { ...item, [field]: value } : item,
      ),
    }
    onChange(autoCode ? { ...next, code: toRoomTypeCode(value) } : next)
  }

  return (
    <Modal centered className="admin-amenity-modal" onHide={onHide} scrollable show size="lg">
      <Modal.Header closeButton>
        <Modal.Title>{roomType.id ? 'Szobatípus szerkesztése' : 'Új szobatípus'}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form
          id="admin-room-type-form"
          onSubmit={(event) => {
            event.preventDefault()
            if (
              hungarian.name.trim() &&
              hungarian.shortDescription.trim() &&
              validCode &&
              validExtraBeds &&
              !saving
            )
              onSave()
          }}
        >
          <div className="admin-amenity-form-grid">
            <Form.Group controlId="room-type-code">
              <Form.Label>Azonosító kód</Form.Label>
              <Form.Control
                isInvalid={Boolean(roomType.code) && !validCode}
                onChange={(event) => onChange({ ...roomType, code: event.target.value })}
                placeholder="peldaul: double"
                readOnly={Boolean(roomType.id)}
                value={roomType.code}
              />
              <Form.Text>
                {roomType.id
                  ? 'A kód létrehozás után nem módosítható.'
                  : 'A magyar névből automatikusan kitöltjük.'}
              </Form.Text>
            </Form.Group>
            <Form.Group controlId="room-type-quantity">
              <Form.Label>Darabszám</Form.Label>
              <Form.Control
                max={15}
                min={0}
                onChange={(event) =>
                  onChange({ ...roomType, quantity: Number(event.target.value) })
                }
                type="number"
                value={roomType.quantity}
              />
              <Form.Text>Ez a foglalásnál választható maximum.</Form.Text>
            </Form.Group>
            <Form.Group controlId="room-type-occupancy">
              <Form.Label>Normál férőhely</Form.Label>
              <Form.Select
                onChange={(event) =>
                  onChange({ ...roomType, standardOccupancy: Number(event.target.value) })
                }
                value={roomType.standardOccupancy}
              >
                {[1, 2, 3, 4].map((value) => (
                  <option key={value} value={value}>
                    {value} ágy
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
            <Form.Group controlId="room-type-extra-count">
              <Form.Label>Pótágyazható szobák száma</Form.Label>
              <Form.Control
                isInvalid={roomType.roomsWithExtraBed > roomType.quantity}
                max={15}
                min={0}
                onChange={(event) =>
                  onChange({ ...roomType, roomsWithExtraBed: Number(event.target.value) })
                }
                type="number"
                value={roomType.roomsWithExtraBed}
              />
            </Form.Group>
            <Form.Group controlId="room-type-extra-per-room">
              <Form.Label>Pótágy / jogosult szoba</Form.Label>
              <Form.Control
                disabled={roomType.roomsWithExtraBed === 0}
                isInvalid={!validExtraBeds}
                max={4}
                min={0}
                onChange={(event) =>
                  onChange({ ...roomType, extraBedsPerEligibleRoom: Number(event.target.value) })
                }
                type="number"
                value={roomType.extraBedsPerEligibleRoom}
              />
            </Form.Group>
          </div>

          <div
            className="admin-amenity-language"
            aria-label="Szobatípus fordításának nyelve"
            role="tablist"
          >
            {(Object.keys(LANGUAGE_LABELS) as RoomTypeLanguage[]).map((code) => (
              <button
                aria-selected={language === code}
                className={language === code ? 'active' : ''}
                key={code}
                onClick={() => setLanguage(code)}
                role="tab"
                type="button"
              >
                {LANGUAGE_LABELS[code]}
              </button>
            ))}
          </div>
          <section className="admin-amenity-translation">
            <p className="admin-eyebrow">{LANGUAGE_LABELS[language]} fordítás</p>
            {language !== 'hu' && !translation.name && (
              <Alert variant="secondary">
                Üresen hagyva a publikus oldalon a magyar szöveg jelenik meg.
              </Alert>
            )}
            <Form.Group controlId="room-type-name">
              <Form.Label>Megnevezés {language === 'hu' && '*'}</Form.Label>
              <Form.Control
                isInvalid={language === 'hu' && !translation.name.trim()}
                onChange={(event) => changeTranslation('name', event.target.value)}
                value={translation.name}
              />
            </Form.Group>
            <Form.Group controlId="room-type-short-description">
              <Form.Label>Rövid leírás {language === 'hu' && '*'}</Form.Label>
              <Form.Control
                as="textarea"
                isInvalid={language === 'hu' && !translation.shortDescription.trim()}
                onChange={(event) => changeTranslation('shortDescription', event.target.value)}
                rows={3}
                value={translation.shortDescription}
              />
            </Form.Group>
            <Form.Group controlId="room-type-detailed-description">
              <Form.Label>Részletes leírás (opcionális)</Form.Label>
              <Form.Control
                as="textarea"
                onChange={(event) => changeTranslation('detailedDescription', event.target.value)}
                rows={4}
                value={translation.detailedDescription}
              />
            </Form.Group>
          </section>
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button disabled={saving} onClick={onHide} variant="outline-secondary">
          <X aria-hidden="true" size={16} /> Mégse
        </Button>
        <Button
          disabled={
            saving ||
            !hungarian.name.trim() ||
            !hungarian.shortDescription.trim() ||
            !validCode ||
            !validExtraBeds
          }
          form="admin-room-type-form"
          type="submit"
        >
          {saving ? (
            <Spinner animation="border" size="sm" />
          ) : (
            <Save aria-hidden="true" size={16} />
          )}
          {saving ? 'Mentés…' : 'Szobatípus mentése'}
        </Button>
      </Modal.Footer>
    </Modal>
  )
}

function translationFor(roomType: AdminRoomType, language: RoomTypeLanguage) {
  return (
    roomType.translations.find((translation) => translation.language === language) ?? {
      language,
      name: '',
      shortDescription: '',
      detailedDescription: '',
    }
  )
}

function copyRoomType(roomType: AdminRoomType): AdminRoomType {
  return {
    ...roomType,
    translations: roomType.translations.map((translation) => ({ ...translation })),
  }
}

function toRoomTypeCode(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
}

function roomTypeErrorMessage(error: unknown) {
  if (error instanceof AdminRoomTypeApiError) {
    if (error.code === 'ROOM_TYPE_CODE_ALREADY_EXISTS')
      return 'Ez az azonosító kód már foglalt ennél a panziónál.'
    if (error.code === 'INVALID_ROOM_TYPE_ORDER')
      return 'A szobatípusok sorrendje időközben megváltozott. Töltsd újra az oldalt.'
    if (error.code === 'ADMIN_ROOM_TYPE_VALIDATION_FAILED')
      return 'Ellenőrizd a darabszámot, férőhelyet és a magyar fordítást.'
  }
  return 'A szobatípus mentése nem sikerült. Próbáld újra.'
}
