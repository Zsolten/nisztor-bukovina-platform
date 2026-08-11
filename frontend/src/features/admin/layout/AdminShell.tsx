import { useState } from 'react'
import { Button, Container, Nav, Navbar, Offcanvas } from 'react-bootstrap'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAdminAuth } from '../auth/adminAuthContext'

export default function AdminShell() {
  const navigate = useNavigate()
  const { logout } = useAdminAuth()
  const [navigationOpen, setNavigationOpen] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)

  async function handleLogout() {
    setLoggingOut(true)
    await logout()
    navigate('/admin/login', { replace: true })
  }

  return (
    <div className="admin-shell">
      <Navbar
        className="admin-header"
        expand="md"
        expanded={navigationOpen}
        onToggle={setNavigationOpen}
      >
        <Container>
          <Navbar.Brand as={NavLink} to="/admin/bookings">
            <strong>Nisztor–Bukovina</strong>
            <span>Admin</span>
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="admin-navigation" aria-label="Admin menü megnyitása" />
          <Navbar.Offcanvas
            aria-label="Admin navigáció"
            id="admin-navigation"
            onHide={() => setNavigationOpen(false)}
            placement="end"
            responsive="md"
          >
            <Offcanvas.Header closeButton closeLabel="Menü bezárása">
              <Offcanvas.Title>Admin menü</Offcanvas.Title>
            </Offcanvas.Header>
            <Offcanvas.Body>
              <Nav className="admin-navigation me-auto">
                <Nav.Link
                  as={NavLink}
                  onClick={() => setNavigationOpen(false)}
                  to="/admin/bookings"
                >
                  Foglalási kérelmek
                </Nav.Link>
              </Nav>
              <Button
                disabled={loggingOut}
                onClick={() => void handleLogout()}
                variant="outline-secondary"
              >
                {loggingOut ? 'Kijelentkezés…' : 'Kijelentkezés'}
              </Button>
            </Offcanvas.Body>
          </Navbar.Offcanvas>
        </Container>
      </Navbar>

      <main className="admin-main">
        <Container>
          <Outlet />
        </Container>
      </main>
    </div>
  )
}
