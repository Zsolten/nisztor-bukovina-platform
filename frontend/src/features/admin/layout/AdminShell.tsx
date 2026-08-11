import { useState } from 'react'
import { Button, Container, Nav, Navbar, Offcanvas } from 'react-bootstrap'
import { ArrowLeft, CalendarDays, LogOut } from 'lucide-react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAdminAuth } from '../auth/adminAuthContext'

export default function AdminShell() {
  const navigate = useNavigate()
  const location = useLocation()
  const { logout } = useAdminAuth()
  const [navigationOpen, setNavigationOpen] = useState(false)
  const [loggingOut, setLoggingOut] = useState(false)
  const isBookingDetail = /^\/admin\/bookings\/[^/]+$/.test(location.pathname)

  async function handleLogout() {
    setLoggingOut(true)
    await logout()
    navigate('/admin/login', { replace: true })
  }

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar" aria-label="Admin navigáció">
        <NavLink className="admin-sidebar-brand" to="/admin/bookings">
          <span className="admin-brand-mark" aria-hidden="true">
            NB
          </span>
          <span className="admin-brand-copy">
            <strong>Nisztor–Bukovina</strong>
            <small>Adminisztráció</small>
          </span>
        </NavLink>

        <nav className="admin-sidebar-navigation">
          <NavLink to="/admin/bookings">
            <CalendarDays aria-hidden="true" size={19} />
            <span>Foglalások</span>
          </NavLink>
        </nav>

        <button
          className="admin-sidebar-logout"
          disabled={loggingOut}
          onClick={() => void handleLogout()}
          type="button"
        >
          <LogOut aria-hidden="true" size={18} />
          <span>{loggingOut ? 'Kijelentkezés…' : 'Kijelentkezés'}</span>
        </button>
      </aside>

      <Navbar
        className="admin-header admin-mobile-header"
        expand={false}
        expanded={navigationOpen}
        onToggle={setNavigationOpen}
      >
        <Container>
          {isBookingDetail ? (
            <div className="admin-mobile-detail-navigation">
              <button
                aria-label="Vissza a foglalásokhoz"
                onClick={() => navigate('/admin/bookings')}
                type="button"
              >
                <ArrowLeft aria-hidden="true" />
              </button>
              <NavLink aria-label="Foglalások" to="/admin/bookings">
                <span className="admin-brand-mark" aria-hidden="true">
                  NB
                </span>
              </NavLink>
            </div>
          ) : (
            <Navbar.Brand as={NavLink} to="/admin/bookings">
              <span className="admin-brand-mark" aria-hidden="true">
                NB
              </span>
              <span className="admin-brand-copy">
                <strong>Nisztor–Bukovina</strong>
                <small>Adminisztráció</small>
              </span>
            </Navbar.Brand>
          )}
          <Navbar.Toggle aria-controls="admin-navigation" aria-label="Admin menü megnyitása" />
          <Navbar.Offcanvas
            aria-label="Admin navigáció"
            id="admin-navigation"
            onHide={() => setNavigationOpen(false)}
            placement="end"
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
                  <CalendarDays aria-hidden="true" size={18} />
                  Foglalások
                </Nav.Link>
              </Nav>
              <Button
                aria-label="Kijelentkezés a mobil menüből"
                className="admin-logout-button"
                disabled={loggingOut}
                onClick={() => void handleLogout()}
                variant="outline-secondary"
              >
                <LogOut aria-hidden="true" size={17} />
                <span>{loggingOut ? 'Kijelentkezés…' : 'Kijelentkezés'}</span>
              </Button>
            </Offcanvas.Body>
          </Navbar.Offcanvas>
        </Container>
      </Navbar>

      <main className="admin-main">
        <Container fluid>
          <Outlet />
        </Container>
      </main>
    </div>
  )
}
