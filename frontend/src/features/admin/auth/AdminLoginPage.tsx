import { useState, type FormEvent } from 'react'
import { Alert, Button, Card, Container, Form } from 'react-bootstrap'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { AdminAuthApiError } from '../api/adminAuth'
import { useAdminAuth } from './adminAuthContext'

interface LoginLocationState {
  from?: string
  sessionEndReason?: 'expired' | 'rejected' | null
}

function loginErrorMessage(error: unknown) {
  if (error instanceof AdminAuthApiError && error.code === 'ADMIN_LOGIN_RATE_LIMITED') {
    return error.retryAfterSeconds
      ? `Túl sok próbálkozás történt. Próbálja újra ${error.retryAfterSeconds} másodperc múlva.`
      : 'Túl sok próbálkozás történt. Kérjük, próbálja újra később.'
  }

  if (error instanceof AdminAuthApiError && error.status === 401) {
    return 'Hibás e-mail-cím vagy jelszó.'
  }

  return 'A bejelentkezés most nem sikerült. Kérjük, próbálja újra.'
}

export default function AdminLoginPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { isAuthenticated, login } = useAdminAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const state = location.state as LoginLocationState | null

  if (isAuthenticated) {
    return <Navigate replace to="/admin/bookings" />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    try {
      await login(email, password)
      const destination = state?.from?.startsWith('/admin/') ? state.from : '/admin/bookings'
      navigate(destination, { replace: true })
    } catch (nextError) {
      setError(loginErrorMessage(nextError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="admin-login-page">
      <Container className="admin-login-container">
        <Card className="admin-login-card" body>
          <p className="admin-eyebrow">Nisztor–Bukovina Panziók</p>
          <h1>Admin belépés</h1>
          <p className="admin-login-introduction">Foglalási kérelmek és tartalmak kezelése.</p>

          {state?.sessionEndReason === 'expired' && (
            <Alert variant="warning">A munkamenet lejárt. Kérjük, jelentkezzen be újra.</Alert>
          )}
          {state?.sessionEndReason === 'rejected' && (
            <Alert variant="warning">
              A munkamenet már nem érvényes. Kérjük, jelentkezzen be újra.
            </Alert>
          )}
          {error && <Alert variant="danger">{error}</Alert>}

          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3" controlId="admin-email">
              <Form.Label>E-mail-cím</Form.Label>
              <Form.Control
                autoComplete="email"
                disabled={submitting}
                onChange={(event) => setEmail(event.target.value)}
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
                onChange={(event) => setPassword(event.target.value)}
                required
                type="password"
                value={password}
              />
            </Form.Group>

            <Button className="w-100" disabled={submitting} type="submit">
              {submitting ? 'Belépés folyamatban…' : 'Belépés'}
            </Button>
          </Form>
        </Card>
      </Container>
    </main>
  )
}
