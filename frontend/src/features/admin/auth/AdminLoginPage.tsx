import { useState, type FormEvent } from 'react'
import { Alert, Button, Card, Container, Form } from 'react-bootstrap'
import { AlertCircle, ArrowRight, ShieldCheck } from 'lucide-react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { AdminAuthApiError } from '../api/adminAuth'
import { useAdminAuth } from './adminAuthContext'

interface LoginLocationState {
  from?: string
  sessionEndReason?: 'expired' | 'rejected' | null
}

interface LoginFeedback {
  message: string
  type: 'credentials' | 'rate-limit' | 'general'
}

function loginErrorFeedback(error: unknown): LoginFeedback {
  if (error instanceof AdminAuthApiError && error.code === 'ADMIN_LOGIN_RATE_LIMITED') {
    return {
      message: error.retryAfterSeconds
        ? `Túl sok próbálkozás történt. Próbálja újra ${error.retryAfterSeconds} másodperc múlva.`
        : 'Túl sok próbálkozás történt. Kérjük, próbálja újra később.',
      type: 'rate-limit',
    }
  }

  if (error instanceof AdminAuthApiError && error.status === 401) {
    return { message: 'Hibás e-mail-cím vagy jelszó.', type: 'credentials' }
  }

  return {
    message: 'A bejelentkezés most nem sikerült. Kérjük, próbálja újra.',
    type: 'general',
  }
}

export default function AdminLoginPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { isAuthenticated, login } = useAdminAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [feedback, setFeedback] = useState<LoginFeedback | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const state = location.state as LoginLocationState | null
  const destination = state?.from?.startsWith('/admin/') ? state.from : '/admin/bookings'

  if (isAuthenticated) {
    return <Navigate replace to={destination} />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setFeedback(null)

    try {
      await login(email, password)
      navigate(destination, { replace: true })
    } catch (nextError) {
      setFeedback(loginErrorFeedback(nextError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="admin-login-page">
      <Container className="admin-login-container">
        <Card className="admin-login-card">
          <section className="admin-login-brand-panel" aria-label="Admin felület bemutatása">
            <span className="admin-login-brand-icon" aria-hidden="true">
              <ShieldCheck size={28} strokeWidth={1.8} />
            </span>
            <p className="admin-eyebrow admin-eyebrow-light">Nisztor–Bukovina Panziók</p>
            <h2>Egy helyen minden fontos teendő.</h2>
            <p>Foglalási kérelmek, tartalmak és árak biztonságos kezelése az admin felületen.</p>
          </section>

          <Card.Body className="admin-login-form-panel">
            <p className="admin-eyebrow">Biztonságos adminisztráció</p>
            <h1>Üdvözöljük újra!</h1>
            <p className="admin-login-introduction">Jelentkezzen be a folytatáshoz.</p>

            {state?.sessionEndReason === 'expired' && (
              <Alert className="admin-login-feedback" variant="warning">
                <div>
                  <strong>A munkamenet lejárt.</strong>
                  <span>Kérjük, jelentkezzen be újra.</span>
                </div>
              </Alert>
            )}
            {state?.sessionEndReason === 'rejected' && (
              <Alert className="admin-login-feedback" variant="warning">
                <div>
                  <strong>A munkamenet már nem érvényes.</strong>
                  <span>Kérjük, jelentkezzen be újra.</span>
                </div>
              </Alert>
            )}
            {feedback && (
              <Alert className="admin-login-feedback" variant="danger">
                <AlertCircle aria-hidden="true" size={21} />
                <div>
                  <strong>Sikertelen bejelentkezés</strong>
                  <span>{feedback.message}</span>
                </div>
              </Alert>
            )}

            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3" controlId="admin-email">
                <Form.Label>E-mail-cím</Form.Label>
                <Form.Control
                  autoComplete="email"
                  autoFocus
                  disabled={submitting}
                  isInvalid={feedback?.type === 'credentials'}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="admin@pelda.hu"
                  required
                  type="email"
                  value={email}
                />
              </Form.Group>

              <Form.Group className="mb-4" controlId="admin-password">
                <Form.Label>Jelszó</Form.Label>
                <Form.Control
                  autoComplete="current-password"
                  disabled={submitting}
                  isInvalid={feedback?.type === 'credentials'}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="Adja meg a jelszavát"
                  required
                  type="password"
                  value={password}
                />
              </Form.Group>

              <Button className="admin-login-submit w-100" disabled={submitting} type="submit">
                <span>{submitting ? 'Belépés folyamatban…' : 'Belépés'}</span>
                {!submitting && <ArrowRight aria-hidden="true" size={19} />}
              </Button>
            </Form>
          </Card.Body>
        </Card>
      </Container>
    </main>
  )
}
