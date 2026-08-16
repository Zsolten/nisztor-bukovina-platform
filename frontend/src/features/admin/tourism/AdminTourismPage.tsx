import { useEffect, useMemo, useState, type FormEvent } from 'react'
import {
  Alert,
  Badge,
  Button,
  Card,
  Col,
  Form,
  Modal,
  Row,
  Spinner,
  Tab,
  Tabs,
} from 'react-bootstrap'
import {
  ChevronDown,
  ChevronUp,
  Clock3,
  MapPinned,
  Pencil,
  Plus,
  Route,
  Trash2,
} from 'lucide-react'
import {
  fetchAttractions,
  fetchStarTours,
  fetchStarTourStopPlan,
  recalculateStarTourRoute,
  saveAttraction,
  saveStarTour,
  saveStarTourStopPlan,
  type AdminAttraction,
  type AdminStarTour,
  type AdminStarTourStop,
  type AdminStarTourStopPlan,
  type AttractionTranslation,
  type AttractionUpdate,
  type StarTourTranslation,
  type StarTourRouteStatus,
  type StarTourUpdate,
} from '../api/adminTourism'
import { useAdminAuth } from '../auth/adminAuthContext'

const emptyAttraction = (): AttractionUpdate => ({
  slug: '',
  latitude: 45.5,
  longitude: 23.2,
  googleMapsUrl: '',
  recommendedVisitDurationMinutes: 60,
  active: true,
  collectionSlugs: [],
  translations: [
    {
      language: 'hu',
      name: '',
      shortDescription: '',
      detailedDescription: '',
      admissionInformation: '',
      practicalInformation: '',
    },
  ],
})

const emptyTour = (): StarTourUpdate => ({
  slug: '',
  mapColor: '#3A6B5C',
  published: false,
  active: true,
  tags: [],
  images: [],
  translations: [{ language: 'hu', name: '', shortDescription: '', detailedDescription: '' }],
})

function huAttraction(item: AttractionUpdate): AttractionTranslation {
  return (
    item.translations.find((translation) => translation.language === 'hu') ??
    emptyAttraction().translations[0]
  )
}

function huTour(item: StarTourUpdate): StarTourTranslation {
  return (
    item.translations.find((translation) => translation.language === 'hu') ??
    emptyTour().translations[0]
  )
}

export default function AdminTourismPage() {
  const { authorizedFetch } = useAdminAuth()
  const [attractions, setAttractions] = useState<AdminAttraction[]>([])
  const [tours, setTours] = useState<AdminStarTour[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [attractionDraft, setAttractionDraft] = useState<AttractionUpdate | null>(null)
  const [attractionId, setAttractionId] = useState<string>()
  const [tourDraft, setTourDraft] = useState<StarTourUpdate | null>(null)
  const [tourId, setTourId] = useState<string>()
  const [stopPlan, setStopPlan] = useState<AdminStarTourStopPlan | null>(null)
  const [stopTour, setStopTour] = useState<AdminStarTour | null>(null)
  const [attractionToAdd, setAttractionToAdd] = useState('')
  const [saving, setSaving] = useState(false)
  const [savingStops, setSavingStops] = useState(false)
  const [recalculatingTourId, setRecalculatingTourId] = useState<string>()

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      fetchAttractions(authorizedFetch, controller.signal),
      fetchStarTours(authorizedFetch, controller.signal),
    ])
      .then(([nextAttractions, nextTours]) => {
        setAttractions(nextAttractions)
        setTours(nextTours)
      })
      .catch((reason: unknown) => {
        if (!controller.signal.aborted)
          setError(reason instanceof Error ? reason.message : 'Betöltési hiba')
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [authorizedFetch])

  const attractionName = useMemo(
    () => (attractionDraft ? huAttraction(attractionDraft) : null),
    [attractionDraft],
  )
  const tourName = useMemo(() => (tourDraft ? huTour(tourDraft) : null), [tourDraft])

  function editAttraction(item?: AdminAttraction) {
    setAttractionId(item?.id)
    setAttractionDraft(item ? { ...item } : emptyAttraction())
    setError('')
    setSuccess('')
  }

  function editTour(item?: AdminStarTour) {
    setTourId(item?.id)
    setTourDraft(item ? { ...item } : emptyTour())
    setError('')
    setSuccess('')
  }

  function updateAttractionHu(patch: Partial<AttractionTranslation>) {
    if (!attractionDraft) return
    setAttractionDraft({
      ...attractionDraft,
      translations: [{ ...huAttraction(attractionDraft), ...patch }],
    })
  }

  function updateTourHu(patch: Partial<StarTourTranslation>) {
    if (!tourDraft) return
    setTourDraft({ ...tourDraft, translations: [{ ...huTour(tourDraft), ...patch }] })
  }

  async function submitAttraction(event: FormEvent) {
    event.preventDefault()
    if (!attractionDraft) return
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const saved = await saveAttraction(authorizedFetch, attractionDraft, attractionId)
      setAttractions((current) => [saved, ...current.filter((item) => item.id !== saved.id)])
      setAttractionDraft(null)
      if (saved.distanceCalculation) {
        const { total, successful, failed } = saved.distanceCalculation
        setSuccess(
          failed === 0
            ? `Látnivaló mentve, ${successful}/${total} távolságpár kiszámolva.`
            : `Látnivaló mentve. ${successful}/${total} távolságpár elkészült, ${failed} számítás sikertelen.`,
        )
      } else {
        setSuccess('Látnivaló mentve.')
      }
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Mentési hiba')
    } finally {
      setSaving(false)
    }
  }

  async function submitTour(event: FormEvent) {
    event.preventDefault()
    if (!tourDraft) return
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      const saved = await saveStarTour(authorizedFetch, tourDraft, tourId)
      setTours((current) => [saved, ...current.filter((item) => item.id !== saved.id)])
      setTourDraft(null)
      setSuccess(
        `Csillagtúra mentve. ${routeStatusMessage(saved.routeStatus, saved.routeFailureReason)}`,
      )
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Mentési hiba')
    } finally {
      setSaving(false)
    }
  }

  async function toggleAttraction(item: AdminAttraction) {
    const saved = await saveAttraction(authorizedFetch, { ...item, active: !item.active }, item.id)
    setAttractions((current) =>
      current.map((candidate) => (candidate.id === saved.id ? saved : candidate)),
    )
  }

  async function toggleTour(item: AdminStarTour) {
    const saved = await saveStarTour(authorizedFetch, { ...item, active: !item.active }, item.id)
    setTours((current) =>
      current.map((candidate) => (candidate.id === saved.id ? saved : candidate)),
    )
  }

  async function recalculateTour(item: AdminStarTour) {
    setRecalculatingTourId(item.id)
    setError('')
    setSuccess('')
    try {
      const saved = await recalculateStarTourRoute(authorizedFetch, item.id)
      setTours((current) =>
        current.map((candidate) => (candidate.id === saved.id ? saved : candidate)),
      )
      setSuccess(routeStatusMessage(saved.routeStatus, saved.routeFailureReason))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Útvonalszámítási hiba')
    } finally {
      setRecalculatingTourId(undefined)
    }
  }

  async function editTourStops(item: AdminStarTour) {
    setError('')
    setSuccess('')
    try {
      const plan = await fetchStarTourStopPlan(authorizedFetch, item.id)
      setStopPlan(plan)
      setStopTour(item)
      setAttractionToAdd('')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'A megállók betöltése nem sikerült')
    }
  }

  function closeStopEditor() {
    setStopPlan(null)
    setStopTour(null)
    setAttractionToAdd('')
  }

  function reportDuplicateStop() {
    closeStopEditor()
    setError('A kiválasztott látnivaló már szerepel ebben a túrában.')
  }

  async function saveStops(stops: AdminStarTourStop[], clearAttractionSelection = false) {
    if (!stopPlan || !stopTour) return
    setSavingStops(true)
    setError('')
    try {
      const saved = await saveStarTourStopPlan(
        authorizedFetch,
        stopPlan.tourId,
        stops.map(({ attractionId, plannedVisitDurationMinutes }) => ({
          attractionId,
          plannedVisitDurationMinutes,
        })),
      )
      setStopPlan(saved)
      setTours((current) =>
        current.map((tour) =>
          tour.id === saved.tourId
            ? { ...tour, published: saved.published, totals: saved.totals }
            : tour,
        ),
      )
      setStopTour((current) =>
        current ? { ...current, published: saved.published, totals: saved.totals } : current,
      )
      if (clearAttractionSelection) setAttractionToAdd('')
    } catch (reason) {
      const message = reason instanceof Error ? reason.message : 'A megállók mentése nem sikerült'
      if (message === 'INVALID_STAR_TOUR_STOPS') reportDuplicateStop()
      else setError(message)
    } finally {
      setSavingStops(false)
    }
  }

  function moveStop(index: number, direction: -1 | 1) {
    if (!stopPlan) return
    const targetIndex = index + direction
    if (targetIndex < 0 || targetIndex >= stopPlan.stops.length) return
    const next = [...stopPlan.stops]
    ;[next[index], next[targetIndex]] = [next[targetIndex], next[index]]
    void saveStops(next)
  }

  function removeStop(index: number) {
    if (!stopPlan) return
    void saveStops(stopPlan.stops.filter((_, candidateIndex) => candidateIndex !== index))
  }

  function addStop() {
    if (!stopPlan || !attractionToAdd) return
    if (stopPlan.assignedAttractionIds.includes(attractionToAdd)) {
      reportDuplicateStop()
      return
    }
    const attraction = attractions.find((item) => item.id === attractionToAdd)
    if (!attraction) return
    void saveStops(
      [
        ...stopPlan.stops,
        {
          attractionId: attraction.id,
          slug: attraction.slug,
          name: huAttraction(attraction).name,
          recommendedVisitDurationMinutes: attraction.recommendedVisitDurationMinutes,
          plannedVisitDurationMinutes: null,
          effectiveVisitDurationMinutes: attraction.recommendedVisitDurationMinutes,
        },
      ],
      true,
    )
  }

  function updateStopDuration(index: number, value: string) {
    if (!stopPlan) return
    const next = stopPlan.stops.map((stop, candidateIndex) =>
      candidateIndex === index
        ? { ...stop, plannedVisitDurationMinutes: value ? Number(value) : null }
        : stop,
    )
    void saveStops(next)
  }

  const availableAttractions = useMemo(
    () =>
      stopPlan
        ? attractions.filter(
            (attraction) => !stopPlan.assignedAttractionIds.includes(attraction.id),
          )
        : [],
    [attractions, stopPlan],
  )

  return (
    <section className="admin-tourism-page">
      <header className="admin-tourism-header">
        <div>
          <p className="admin-eyebrow">Turisztikai katalógus</p>
          <h1>Csillagtúrák és látnivalók</h1>
          <p>
            A magyar tartalom most szerkeszthető; a háttér készen áll a román és angol fordításokra.
          </p>
        </div>
      </header>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}
      {loading ? (
        <div className="admin-tourism-loading">
          <Spinner animation="border" /> Betöltés…
        </div>
      ) : (
        <Tabs defaultActiveKey="attractions" className="admin-tourism-tabs">
          <Tab eventKey="attractions" title={`Látnivalók (${attractions.length})`}>
            <CatalogueHeader title="Látnivalók" onAdd={() => editAttraction()} />
            <Row className="g-3">
              {attractions.map((item) => (
                <Col key={item.id} lg={6} xl={4}>
                  <Card className="admin-tourism-card h-100">
                    <Card.Body>
                      <div className="admin-tourism-card-heading">
                        <MapPinned aria-hidden="true" />
                        <div>
                          <Card.Title>{huAttraction(item).name}</Card.Title>
                          <code>{item.slug}</code>
                        </div>
                        <Badge bg={item.active ? 'success' : 'secondary'}>
                          {item.active ? 'Aktív' : 'Inaktív'}
                        </Badge>
                      </div>
                      <Card.Text>{huAttraction(item).shortDescription}</Card.Text>
                      <small>
                        Ajánlott látogatási idő:{' '}
                        {formatDuration(item.recommendedVisitDurationMinutes)}
                      </small>
                      <small>
                        {item.latitude}, {item.longitude}
                      </small>
                    </Card.Body>
                    <Card.Footer>
                      <Button
                        size="sm"
                        variant="outline-secondary"
                        onClick={() => void toggleAttraction(item)}
                      >
                        {item.active ? 'Deaktiválás' : 'Aktiválás'}
                      </Button>
                      <Button size="sm" onClick={() => editAttraction(item)}>
                        <Pencil size={15} /> Szerkesztés
                      </Button>
                    </Card.Footer>
                  </Card>
                </Col>
              ))}
            </Row>
          </Tab>
          <Tab eventKey="tours" title={`Csillagtúrák (${tours.length})`}>
            <CatalogueHeader title="Csillagtúrák" onAdd={() => editTour()} />
            {tours.length === 0 && (
              <Alert variant="light">
                Még nincs csillagtúra. Hozd létre az első útvonal alapadatait.
              </Alert>
            )}
            <Row className="g-3">
              {tours.map((item) => (
                <Col key={item.id} lg={6} xl={4}>
                  <Card
                    className="admin-tourism-card h-100"
                    style={{ borderTopColor: item.mapColor }}
                  >
                    <Card.Body>
                      <div className="admin-tourism-card-heading">
                        <Route aria-hidden="true" style={{ color: item.mapColor }} />
                        <div>
                          <Card.Title>{huTour(item).name}</Card.Title>
                          <code>{item.slug}</code>
                        </div>
                        <Badge bg={item.published && item.active ? 'success' : 'secondary'}>
                          {item.published ? (item.active ? 'Publikus' : 'Inaktív') : 'Vázlat'}
                        </Badge>
                      </div>
                      <Card.Text>{huTour(item).shortDescription}</Card.Text>
                      <small className="admin-tourism-total">
                        {item.totals.routeDataComplete
                          ? `${formatDistance(item.totals.travelDistanceMeters)} · ${formatDuration(item.totals.totalDurationSeconds ? item.totals.totalDurationSeconds / 60 : 0)}`
                          : 'A közzétételhez hiányzik útvonalszakasz-adat'}
                      </small>
                      <Badge bg={routeStatusVariant(item.routeStatus)}>
                        {routeStatusLabel(item.routeStatus)}
                      </Badge>
                    </Card.Body>
                    <Card.Footer>
                      <Button
                        size="sm"
                        variant="outline-primary"
                        onClick={() => void editTourStops(item)}
                      >
                        <Route size={15} /> Megállók
                      </Button>
                      <Button
                        size="sm"
                        variant="outline-primary"
                        disabled={recalculatingTourId === item.id}
                        onClick={() => void recalculateTour(item)}
                      >
                        {recalculatingTourId === item.id ? 'Számítás…' : 'Útvonal újraszámítása'}
                      </Button>
                      <Button
                        size="sm"
                        variant="outline-secondary"
                        onClick={() => void toggleTour(item)}
                      >
                        {item.active ? 'Deaktiválás' : 'Aktiválás'}
                      </Button>
                      <Button size="sm" onClick={() => editTour(item)}>
                        <Pencil size={15} /> Szerkesztés
                      </Button>
                    </Card.Footer>
                  </Card>
                </Col>
              ))}
            </Row>
          </Tab>
        </Tabs>
      )}

      <Modal show={Boolean(attractionDraft)} onHide={() => setAttractionDraft(null)} size="lg">
        <Form onSubmit={(event) => void submitAttraction(event)}>
          <Modal.Header closeButton>
            <Modal.Title>{attractionId ? 'Látnivaló szerkesztése' : 'Új látnivaló'}</Modal.Title>
          </Modal.Header>
          {attractionDraft && attractionName && (
            <Modal.Body className="admin-tourism-form">
              <Row className="g-3">
                <Col md={8}>
                  <Form.Group>
                    <Form.Label>Cím</Form.Label>
                    <Form.Control
                      required
                      value={attractionName.name}
                      onChange={(e) => updateAttractionHu({ name: e.target.value })}
                    />
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Slug</Form.Label>
                    <Form.Control
                      required
                      pattern="[a-z0-9]+(?:-[a-z0-9]+)*"
                      value={attractionDraft.slug}
                      onChange={(e) =>
                        setAttractionDraft({ ...attractionDraft, slug: e.target.value })
                      }
                    />
                  </Form.Group>
                </Col>
              </Row>
              <Form.Group>
                <Form.Label>Rövid leírás</Form.Label>
                <Form.Control
                  required
                  as="textarea"
                  rows={2}
                  value={attractionName.shortDescription}
                  onChange={(e) => updateAttractionHu({ shortDescription: e.target.value })}
                />
              </Form.Group>
              <Form.Group>
                <Form.Label>Hosszú leírás</Form.Label>
                <Form.Control
                  required
                  as="textarea"
                  rows={7}
                  value={attractionName.detailedDescription}
                  onChange={(e) => updateAttractionHu({ detailedDescription: e.target.value })}
                />
              </Form.Group>
              <Row className="g-3">
                <Col md={6}>
                  <Form.Group>
                    <Form.Label>Belépőinformáció</Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={2}
                      value={attractionName.admissionInformation ?? ''}
                      onChange={(e) => updateAttractionHu({ admissionInformation: e.target.value })}
                    />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label>Gyakorlati információ</Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={2}
                      value={attractionName.practicalInformation ?? ''}
                      onChange={(e) => updateAttractionHu({ practicalInformation: e.target.value })}
                    />
                  </Form.Group>
                </Col>
              </Row>
              <Row className="g-3">
                <Col md={6}>
                  <Form.Group>
                    <Form.Label>Ajánlott látogatási idő (perc)</Form.Label>
                    <Form.Control
                      required
                      type="number"
                      min={5}
                      max={720}
                      step={5}
                      value={attractionDraft.recommendedVisitDurationMinutes}
                      onChange={(e) =>
                        setAttractionDraft({
                          ...attractionDraft,
                          recommendedVisitDurationMinutes: Number(e.target.value),
                        })
                      }
                    />
                    <Form.Text>A csillagtúrák ezt használják alapértelmezettként.</Form.Text>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label>Szélesség</Form.Label>
                    <Form.Control
                      required
                      type="number"
                      step="any"
                      min={-90}
                      max={90}
                      value={attractionDraft.latitude}
                      onChange={(e) =>
                        setAttractionDraft({ ...attractionDraft, latitude: Number(e.target.value) })
                      }
                    />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label>Hosszúság</Form.Label>
                    <Form.Control
                      required
                      type="number"
                      step="any"
                      min={-180}
                      max={180}
                      value={attractionDraft.longitude}
                      onChange={(e) =>
                        setAttractionDraft({
                          ...attractionDraft,
                          longitude: Number(e.target.value),
                        })
                      }
                    />
                  </Form.Group>
                </Col>
              </Row>
              <Form.Group>
                <Form.Label>Google Maps link</Form.Label>
                <Form.Control
                  required
                  type="url"
                  value={attractionDraft.googleMapsUrl}
                  onChange={(e) =>
                    setAttractionDraft({ ...attractionDraft, googleMapsUrl: e.target.value })
                  }
                />
              </Form.Group>
              <Form.Group>
                <Form.Label>Gyűjteménykulcsok</Form.Label>
                <Form.Control
                  value={attractionDraft.collectionSlugs.join(', ')}
                  onChange={(e) =>
                    setAttractionDraft({
                      ...attractionDraft,
                      collectionSlugs: e.target.value
                        .split(',')
                        .map((value) => value.trim())
                        .filter(Boolean),
                    })
                  }
                />
                <Form.Text>Vesszővel elválasztva, például: maros-mente, arany-nyomaban</Form.Text>
              </Form.Group>
              <Form.Check
                checked={attractionDraft.active}
                onChange={(e) =>
                  setAttractionDraft({ ...attractionDraft, active: e.target.checked })
                }
                label="Aktív, megjelenhet a publikus API-ban"
              />
            </Modal.Body>
          )}
          <Modal.Footer>
            <Button variant="outline-secondary" onClick={() => setAttractionDraft(null)}>
              Mégse
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Mentés…' : 'Mentés'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      <Modal
        show={Boolean(stopPlan && stopTour)}
        onHide={() => {
          if (!savingStops) closeStopEditor()
        }}
        size="xl"
      >
        <Modal.Header closeButton>
          <Modal.Title>Megállók és időterv: {stopTour ? huTour(stopTour).name : ''}</Modal.Title>
        </Modal.Header>
        {stopPlan && (
          <Modal.Body className="admin-tour-stop-editor">
            <section className="admin-tour-stop-list">
              <div className="admin-tour-stop-heading">
                <div>
                  <p className="admin-eyebrow">Fő útvonal</p>
                  <h3>Megállók sorrendje</h3>
                </div>
                <span>{stopPlan.stops.length} megálló</span>
              </div>

              <div className="admin-tour-stop-add">
                <Form.Select
                  aria-label="Látnivaló hozzáadása"
                  value={attractionToAdd}
                  disabled={savingStops || availableAttractions.length === 0}
                  onChange={(event) => setAttractionToAdd(event.target.value)}
                >
                  <option value="">Látnivaló hozzáadása...</option>
                  {availableAttractions.map((attraction) => (
                    <option key={attraction.id} value={attraction.id}>
                      {huAttraction(attraction).name}
                    </option>
                  ))}
                </Form.Select>
                <Button
                  aria-label="Látnivaló hozzáadása"
                  disabled={savingStops || !attractionToAdd}
                  onClick={addStop}
                >
                  <Plus size={17} /> Hozzáadás
                </Button>
              </div>

              {stopPlan.stops.length === 0 ? (
                <Alert variant="light" className="mb-0">
                  Adj hozzá legalább egy fő megállót az útvonalhoz.
                </Alert>
              ) : (
                <ol className="admin-tour-stop-items">
                  {stopPlan.stops.map((stop, index) => (
                    <li key={stop.attractionId}>
                      <div className="admin-tour-stop-order">{index + 1}</div>
                      <div className="admin-tour-stop-name">
                        <strong>{stop.name}</strong>
                        <span>
                          Ajánlott: {formatDuration(stop.recommendedVisitDurationMinutes)}
                        </span>
                      </div>
                      <Form.Select
                        aria-label={`${stop.name} látogatási ideje`}
                        value={stop.plannedVisitDurationMinutes ?? ''}
                        disabled={savingStops}
                        onChange={(event) => updateStopDuration(index, event.target.value)}
                      >
                        <option value="">Ajánlott idő</option>
                        {[30, 45, 60, 90, 120, 180, 240].map((minutes) => (
                          <option key={minutes} value={minutes}>
                            {formatDuration(minutes)}
                          </option>
                        ))}
                      </Form.Select>
                      <div className="admin-tour-stop-actions">
                        <Button
                          aria-label={`${stop.name} feljebb`}
                          disabled={savingStops || index === 0}
                          onClick={() => moveStop(index, -1)}
                          size="sm"
                          variant="outline-secondary"
                        >
                          <ChevronUp size={16} />
                        </Button>
                        <Button
                          aria-label={`${stop.name} lejjebb`}
                          disabled={savingStops || index === stopPlan.stops.length - 1}
                          onClick={() => moveStop(index, 1)}
                          size="sm"
                          variant="outline-secondary"
                        >
                          <ChevronDown size={16} />
                        </Button>
                        <Button
                          aria-label={`${stop.name} eltávolítása`}
                          disabled={savingStops}
                          onClick={() => removeStop(index)}
                          size="sm"
                          variant="outline-danger"
                        >
                          <Trash2 size={16} />
                        </Button>
                      </div>
                    </li>
                  ))}
                </ol>
              )}
            </section>

            <aside className="admin-tour-summary" aria-live="polite">
              <p className="admin-eyebrow">Automatikus összesítés</p>
              <h3>Tervezett nap</h3>
              <dl>
                <div>
                  <dt>
                    <Route size={16} /> Megállók között
                  </dt>
                  <dd>
                    {stopPlan.totals.routeDataComplete
                      ? `${formatDistance(stopPlan.totals.travelDistanceMeters)} · ${formatDuration((stopPlan.totals.travelDurationSeconds ?? 0) / 60)}`
                      : 'Nem számolható'}
                  </dd>
                </div>
                <div>
                  <dt>
                    <Clock3 size={16} /> Látogatások
                  </dt>
                  <dd>{formatDuration(stopPlan.totals.visitDurationMinutes)}</dd>
                </div>
                <div className="admin-tour-summary-total">
                  <dt>Fő útvonal ideje</dt>
                  <dd>
                    {stopPlan.totals.totalDurationSeconds === null
                      ? 'Nem számolható'
                      : formatDuration(stopPlan.totals.totalDurationSeconds / 60)}
                  </dd>
                </div>
              </dl>

              {!stopPlan.totals.routeDataComplete && (
                <Alert variant="warning">
                  A túra jelenleg nem tehető közzé, mert az alábbi fő szakaszokhoz nincs kész
                  útvonaladat.
                </Alert>
              )}
              {stopPlan.totals.routeLegs.some((leg) => leg.status !== 'SUCCESS') && (
                <ul className="admin-tour-leg-status">
                  {stopPlan.totals.routeLegs
                    .filter((leg) => leg.status !== 'SUCCESS')
                    .map((leg) => (
                      <li key={`${leg.fromSlug}-${leg.toSlug}`}>
                        {leg.fromSlug} → {leg.toSlug}:{' '}
                        {leg.status === 'MISSING' ? 'nincs adat' : 'sikertelen számítás'}
                      </li>
                    ))}
                </ul>
              )}
              {stopPlan.published && stopPlan.totals.routeDataComplete && (
                <Alert variant="success">A közzétett túra útvonaladata teljes.</Alert>
              )}
            </aside>
          </Modal.Body>
        )}
        <Modal.Footer>
          <Button
            variant="outline-secondary"
            disabled={savingStops}
            onClick={closeStopEditor}
          >
            Bezárás
          </Button>
        </Modal.Footer>
      </Modal>

      <Modal show={Boolean(tourDraft)} onHide={() => setTourDraft(null)} size="lg">
        <Form onSubmit={(event) => void submitTour(event)}>
          <Modal.Header closeButton>
            <Modal.Title>{tourId ? 'Csillagtúra szerkesztése' : 'Új csillagtúra'}</Modal.Title>
          </Modal.Header>
          {tourDraft && tourName && (
            <Modal.Body className="admin-tourism-form">
              <Row className="g-3">
                <Col md={8}>
                  <Form.Group>
                    <Form.Label>Név</Form.Label>
                    <Form.Control
                      required
                      value={tourName.name}
                      onChange={(e) => updateTourHu({ name: e.target.value })}
                    />
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Slug</Form.Label>
                    <Form.Control
                      required
                      pattern="[a-z0-9]+(?:-[a-z0-9]+)*"
                      value={tourDraft.slug}
                      onChange={(e) => setTourDraft({ ...tourDraft, slug: e.target.value })}
                    />
                  </Form.Group>
                </Col>
              </Row>
              <Form.Group>
                <Form.Label>Rövid leírás</Form.Label>
                <Form.Control
                  required
                  as="textarea"
                  rows={2}
                  value={tourName.shortDescription}
                  onChange={(e) => updateTourHu({ shortDescription: e.target.value })}
                />
              </Form.Group>
              <Form.Group>
                <Form.Label>Hosszú leírás</Form.Label>
                <Form.Control
                  required
                  as="textarea"
                  rows={7}
                  value={tourName.detailedDescription}
                  onChange={(e) => updateTourHu({ detailedDescription: e.target.value })}
                />
              </Form.Group>
              <Row className="g-3">
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Térképszín</Form.Label>
                    <Form.Control
                      type="color"
                      value={tourDraft.mapColor}
                      onChange={(e) => setTourDraft({ ...tourDraft, mapColor: e.target.value })}
                    />
                  </Form.Group>
                </Col>
                <Col md={8}>
                  <Form.Group>
                    <Form.Label>Címkék</Form.Label>
                    <Form.Control
                      value={tourDraft.tags.join(', ')}
                      onChange={(e) =>
                        setTourDraft({
                          ...tourDraft,
                          tags: e.target.value
                            .split(',')
                            .map((value) => value.trim())
                            .filter(Boolean),
                        })
                      }
                    />
                  </Form.Group>
                </Col>
              </Row>
              <Form.Group>
                <Form.Label>Képek</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  value={tourDraft.images
                    .map((image) => `${image.imageUrl} | ${image.altText}`)
                    .join('\n')}
                  onChange={(e) =>
                    setTourDraft({
                      ...tourDraft,
                      images: e.target.value
                        .split('\n')
                        .filter(Boolean)
                        .map((line) => {
                          const [imageUrl, altText = ''] = line.split('|')
                          return { imageUrl: imageUrl.trim(), altText: altText.trim() }
                        }),
                    })
                  }
                />
                <Form.Text>Soronként: kép URL | magyar alternatív szöveg</Form.Text>
              </Form.Group>
              <div className="d-flex gap-4">
                <Form.Check
                  checked={tourDraft.published}
                  onChange={(e) => setTourDraft({ ...tourDraft, published: e.target.checked })}
                  label="Publikált"
                />
                <Form.Check
                  checked={tourDraft.active}
                  onChange={(e) => setTourDraft({ ...tourDraft, active: e.target.checked })}
                  label="Aktív"
                />
              </div>
            </Modal.Body>
          )}
          <Modal.Footer>
            <Button variant="outline-secondary" onClick={() => setTourDraft(null)}>
              Mégse
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Mentés…' : 'Mentés'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </section>
  )
}

function routeStatusLabel(status: StarTourRouteStatus) {
  return {
    READY: 'Útvonal kész',
    MISSING: 'Nincs útvonal',
    STALE: 'Újraszámítás szükséges',
    CALCULATING: 'Számítás folyamatban',
    FAILED: 'Útvonalhiba',
  }[status]
}

function formatDuration(minutes: number) {
  const roundedMinutes = Math.round(minutes)
  const hours = Math.floor(roundedMinutes / 60)
  const remainingMinutes = roundedMinutes % 60
  if (hours === 0) return `${remainingMinutes} perc`
  if (remainingMinutes === 0) return `${hours} óra`
  return `${hours} óra ${remainingMinutes} perc`
}

function formatDistance(distanceMeters: number | null) {
  if (distanceMeters === null) return 'Nincs adat'
  return `${(distanceMeters / 1000).toLocaleString('hu-HU', { maximumFractionDigits: 1 })} km`
}

function routeStatusVariant(status: StarTourRouteStatus) {
  return {
    READY: 'success',
    MISSING: 'secondary',
    STALE: 'warning',
    CALCULATING: 'info',
    FAILED: 'danger',
  }[status]
}

function routeStatusMessage(status: StarTourRouteStatus, failureReason?: string | null) {
  if (status === 'READY') return 'Az útvonal kiszámolva és kirajzolható.'
  if (status === 'MISSING') return 'A túra mentve, de nincs elegendő aktív megálló az útvonalhoz.'
  if (status === 'CALCULATING') return 'Az útvonal számítása folyamatban van.'
  if (status === 'STALE') return 'A túra mentve, az útvonal újraszámítása szükséges.'
  return `A túra mentve, de az útvonal nem rajzolható ki${failureReason ? `: ${failureReason}` : '.'}`
}

function CatalogueHeader({ title, onAdd }: { title: string; onAdd: () => void }) {
  return (
    <div className="admin-tourism-section-heading">
      <h2>{title}</h2>
      <Button onClick={onAdd}>
        <Plus size={17} /> Új létrehozása
      </Button>
    </div>
  )
}
