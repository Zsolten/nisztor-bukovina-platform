import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAdminAuth } from './adminAuthContext'

export default function ProtectedAdminRoute() {
  const location = useLocation()
  const { isAuthenticated, sessionEndReason } = useAdminAuth()

  if (!isAuthenticated) {
    return (
      <Navigate replace to="/admin/login" state={{ from: location.pathname, sessionEndReason }} />
    )
  }

  return <Outlet />
}
