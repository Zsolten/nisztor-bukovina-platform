import { createContext, useContext } from 'react'

export type AdminSessionEndReason = 'expired' | 'rejected' | null

interface AdminAuthContextValue {
  accessToken: string | null
  authorizedFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>
  clearRejectedSession: () => void
  expiresAt: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  sessionEndReason: AdminSessionEndReason
}

export const AdminAuthContext = createContext<AdminAuthContextValue | null>(null)

export function useAdminAuth() {
  const context = useContext(AdminAuthContext)
  if (!context) {
    throw new Error('useAdminAuth must be used inside AdminAuthProvider')
  }
  return context
}
