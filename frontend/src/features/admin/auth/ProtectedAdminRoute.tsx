import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAdminAuth } from './adminAuthContext'

export default function ProtectedAdminRoute() {
  const location = useLocation()
  const { isAuthenticated, sessionEndReason } = useAdminAuth()

  if (!isAuthenticated) {
    const returnPath = `${location.pathname}${location.search}${location.hash}`
    return <Navigate replace to="/admin/login" state={{ from: returnPath, sessionEndReason }} />
  }

  return <Outlet />
}
