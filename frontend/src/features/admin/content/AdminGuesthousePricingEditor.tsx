import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Button, Form, Spinner } from 'react-bootstrap'
import { Check, Save } from 'lucide-react'
import {
  AdminGuesthousePricingApiError,
  fetchAdminGuesthousePricing,
  type AdminGuesthousePricing,
  updateAdminGuesthousePricing,
} from '../api/adminGuesthousePricing'
import { useAdminAuth } from '../auth/adminAuthContext'

interface AdminGuesthousePricingEditorProps {
  guesthouseId: string
  guesthouseName: string
  onDirtyChange?: (dirty: boolean) => void
  onSaved?: () => void
}

type AmountDraft = Record<string, string>

const UNIT_LABELS = {
  person_night: 'RON / fő / éj',
  room_night: 'RON / szoba / éj',
  person: 'RON / fő',
  day: 'RON / nap',
} as const

const MEAL_CODES = new Set([
  'breakfast',
  'lunch',
  'dinner',
  'bed_and_breakfast',
  'half_board',
  'full_board',
])

export default function AdminGuesthousePricingEditor({
  guesthouseId,
  guesthouseName,
  onDirtyChange,
  onSaved,
}: AdminGuesthousePricingEditorProps) {
  const { authorizedFetch } = useAdminAuth()
  const [pricing, setPricing] = useState<AdminGuesthousePricing | null>(null)
  const [draft, setDraft] = useState<AmountDraft>({})
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [feedback, setFeedback] = useState<{
    variant: 'success' | 'danger'
    message: string
  } | null>(null)

  const load = useCallback(
    async (signal?: AbortSignal) => {
      setLoading(true)
      setFeedback(null)
      try {
        const result = await fetchAdminGuesthousePricing(authorizedFetch, guesthouseId, signal)
        if (!isPricing(result)) throw new Error('Invalid pricing response')
        setPricing(result)
        setDraft(toDraft(result))
        onDirtyChange?.(false)
      } catch (error) {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setFeedback({ variant: 'danger', message: 'Az árak nem tölthetők be. Próbáld újra.' })
        }
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [authorizedFetch, guesthouseId, onDirtyChange],
  )

  useEffect(() => {
    const controller = new AbortController()
    const timeout = window.setTimeout(() => void load(controller.signal), 0)
    return () => {
      window.clearTimeout(timeout)
      controller.abort()
    }
  }, [load])

  const dirty = useMemo(
    () => (pricing ? JSON.stringify(draft) !== JSON.stringify(toDraft(pricing)) : false),
    [draft, pricing],
  )
  const groups = useMemo(() => (pricing ? groupItems(pricing.items) : []), [pricing])

  function setValue(key: string, value: string) {
    setDraft((current) => ({ ...current, [key]: value }))
    setFeedback(null)
  }

  async function save() {
    if (!pricing) return
    const payload = toUpdate(pricing, draft)
    if (!payload) {
      setFeedback({
        variant: 'danger',
        message: 'Minden árnak nulla vagy pozitív számnak kell lennie.',
      })
      return
    }

    setSaving(true)
    setFeedback(null)
    try {
      const result = await updateAdminGuesthousePricing(authorizedFetch, guesthouseId, payload)
      setPricing(result)
      setDraft(toDraft(result))
      onDirtyChange?.(false)
      onSaved?.()
      setFeedback({
        variant: 'success',
        message: 'Az árak mentése sikerült. Az új foglalási kalkulációk már ezeket használják.',
      })
    } catch (error) {
      const message =
        error instanceof AdminGuesthousePricingApiError &&
        error.code === 'ADMIN_PRICING_VALIDATION_FAILED'
          ? 'A szerver elutasította az árakat. Ellenőrizd a megadott értékeket.'
          : 'Az árak mentése nem sikerült. Próbáld újra.'
      setFeedback({ variant: 'danger', message })
    } finally {
      setSaving(false)
    }
  }

  useEffect(() => {
    onDirtyChange?.(dirty)
  }, [dirty, onDirtyChange])

  if (loading) {
    return (
      <div className="admin-pricing-status" role="status">
        <Spinner animation="border" size="sm" /> Árak betöltése…
      </div>
    )
  }

  if (!pricing) {
    return (
      <Alert className="admin-pricing-status" variant="danger">
        {feedback?.message ?? 'Nincs szerkeszthető árlista.'}
        <Button className="ms-3" onClick={() => void load()} size="sm" variant="outline-danger">
          Újrapróbálás
        </Button>
      </Alert>
    )
  }

  return (
    <section className="admin-pricing-editor" aria-labelledby="admin-pricing-heading">
      <header className="admin-pricing-heading">
        <div>
          <p className="admin-eyebrow">Aktuális árak</p>
          <h3 id="admin-pricing-heading">{guesthouseName} árkezelése</h3>
          <p>
            Csak a most érvényes összegek szerkeszthetők. A korábbi foglalások árösszesítője nem
            változik.
          </p>
        </div>
        <span>{pricing.currency}</span>
      </header>

      {feedback && (
        <Alert variant={feedback.variant}>
          {feedback.variant === 'success' && <Check aria-hidden="true" size={17} />}{' '}
          {feedback.message}
        </Alert>
      )}

      <div className="admin-pricing-groups">
        {groups.map((group) => (
          <section className="admin-pricing-group" key={group.title}>
            <h4>{group.title}</h4>
            {group.items.map((item) => (
              <Form.Group controlId={`price-${item.code}`} key={item.code}>
                <Form.Label>{item.label}</Form.Label>
                <div className="admin-pricing-input">
                  <Form.Control
                    aria-label={item.label}
                    inputMode="decimal"
                    min="0"
                    onChange={(event) => setValue(`item:${item.code}`, event.target.value)}
                    step="0.01"
                    type="number"
                    value={draft[`item:${item.code}`] ?? ''}
                  />
                  <span>{UNIT_LABELS[item.unit]}</span>
                </div>
              </Form.Group>
            ))}
            {group.title === 'Szállás' && (
              <p className="admin-pricing-fixed-note">0–3 éves gyermek: ingyenes</p>
            )}
          </section>
        ))}

        {pricing.surcharges.length > 0 && (
          <AdjustmentGroup
            adjustments={pricing.surcharges}
            draft={draft}
            onChange={setValue}
            title="Felárak"
            type="surcharge"
          />
        )}
        {pricing.discounts.length > 0 && (
          <AdjustmentGroup
            adjustments={pricing.discounts}
            draft={draft}
            onChange={setValue}
            title="Kedvezmények"
            type="discount"
          />
        )}
      </div>

      <footer className="admin-pricing-save">
        <span>{dirty ? 'Nem mentett módosítás' : 'Minden változtatás mentve'}</span>
        <Button disabled={!dirty || saving} onClick={() => void save()}>
          {saving ? (
            <Spinner animation="border" size="sm" />
          ) : (
            <Save aria-hidden="true" size={17} />
          )}
          {saving ? 'Mentés…' : 'Árak mentése'}
        </Button>
      </footer>
    </section>
  )
}

function AdjustmentGroup({
  adjustments,
  draft,
  onChange,
  title,
  type,
}: {
  adjustments: AdminGuesthousePricing['surcharges']
  draft: AmountDraft
  onChange: (key: string, value: string) => void
  title: string
  type: 'surcharge' | 'discount'
}) {
  return (
    <section className="admin-pricing-group">
      <h4>{title}</h4>
      {adjustments.map((adjustment) => (
        <Form.Group controlId={`${type}-${adjustment.code}`} key={adjustment.code}>
          <Form.Label>{adjustment.label}</Form.Label>
          <div className="admin-pricing-input">
            <Form.Control
              aria-label={adjustment.label}
              inputMode="decimal"
              min="0"
              max="100"
              onChange={(event) => onChange(`${type}:${adjustment.code}`, event.target.value)}
              step="0.01"
              type="number"
              value={draft[`${type}:${adjustment.code}`] ?? ''}
            />
            <span>%</span>
          </div>
        </Form.Group>
      ))}
    </section>
  )
}

function groupItems(items: AdminGuesthousePricing['items']) {
  const accommodation = items.filter(
    (item) => item.code === 'accommodation' || item.code === 'single_room',
  )
  const meals = items.filter((item) => MEAL_CODES.has(item.code))
  const known = new Set([...accommodation, ...meals].map((item) => item.code))
  const other = items.filter((item) => !known.has(item.code))
  return [
    { title: 'Szállás', items: accommodation },
    { title: 'Étkezések', items: meals },
    { title: 'Egyéb árak', items: other },
  ].filter((group) => group.items.length > 0)
}

function toDraft(pricing: AdminGuesthousePricing): AmountDraft {
  return Object.fromEntries([
    ...pricing.items.map((item) => [`item:${item.code}`, String(item.amount)]),
    ...pricing.surcharges.map((adjustment) => [
      `surcharge:${adjustment.code}`,
      String(adjustment.percentage),
    ]),
    ...pricing.discounts.map((adjustment) => [
      `discount:${adjustment.code}`,
      String(adjustment.percentage),
    ]),
  ])
}

function toUpdate(pricing: AdminGuesthousePricing, draft: AmountDraft) {
  const number = (value: string | undefined) => Number(value)
  const items = pricing.items.map((item) => ({
    code: item.code,
    amount: number(draft[`item:${item.code}`]),
  }))
  const surcharges = pricing.surcharges.map((item) => ({
    code: item.code,
    percentage: number(draft[`surcharge:${item.code}`]),
  }))
  const discounts = pricing.discounts.map((item) => ({
    code: item.code,
    percentage: number(draft[`discount:${item.code}`]),
  }))
  if (
    ![
      ...items.map((item) => item.amount),
      ...surcharges.map((item) => item.percentage),
      ...discounts.map((item) => item.percentage),
    ].every((value) => Number.isFinite(value) && value >= 0 && value <= 1000000)
  )
    return null
  if (![...surcharges, ...discounts].every((item) => item.percentage <= 100)) return null
  return { items, surcharges, discounts }
}

function isPricing(value: AdminGuesthousePricing): value is AdminGuesthousePricing {
  return (
    Array.isArray(value.items) &&
    Array.isArray(value.surcharges) &&
    Array.isArray(value.discounts) &&
    typeof value.currency === 'string'
  )
}
