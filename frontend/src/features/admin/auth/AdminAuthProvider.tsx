import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { loginAdmin, logoutAdmin } from '../api/adminAuth'
import { AdminAuthContext, type AdminSessionEndReason } from './adminAuthContext'

interface AdminSession {
  accessToken: string
  expiresAt: string
}

interface AdminAuthProviderProps {
  children: ReactNode
}

class AdminSessionRejectedError extends Error {
  constructor() {
    super('Administrator session is no longer valid')
  }
}

export function AdminAuthProvider({ children }: AdminAuthProviderProps) {
  const [session, setSession] = useState<AdminSession | null>(null)
  const [sessionEndReason, setSessionEndReason] = useState<AdminSessionEndReason>(null)

  const clearSession = useCallback((reason: AdminSessionEndReason = null) => {
    setSession(null)
    setSessionEndReason(reason)
  }, [])

  useEffect(() => {
    if (!session) {
      return undefined
    }

    const millisecondsUntilExpiry = Date.parse(session.expiresAt) - Date.now()
    const timeout = window.setTimeout(
      () => clearSession('expired'),
      Math.max(0, millisecondsUntilExpiry),
    )
    return () => window.clearTimeout(timeout)
  }, [clearSession, session])

  const login = useCallback(async (email: string, password: string) => {
    const nextSession = await loginAdmin(email, password)
    setSession({ accessToken: nextSession.accessToken, expiresAt: nextSession.expiresAt })
    setSessionEndReason(null)
  }, [])

  const logout = useCallback(async () => {
    const accessToken = session?.accessToken
    clearSession()

    if (accessToken) {
      try {
        await logoutAdmin(accessToken)
      } catch {
        // Local access is already removed even when the logout request cannot complete.
      }
    }
  }, [clearSession, session])

  const authorizedFetch = useCallback(
    async (input: RequestInfo | URL, init?: RequestInit) => {
      if (!session) {
        throw new AdminSessionRejectedError()
      }

      const headers = new Headers(init?.headers)
      headers.set('Authorization', `Bearer ${session.accessToken}`)
      const response = await fetch(input, { ...init, headers })

      if (response.status === 401 || response.status === 403) {
        clearSession('rejected')
        throw new AdminSessionRejectedError()
      }

      return response
    },
    [clearSession, session],
  )

  const value = useMemo(
    () => ({
      accessToken: session?.accessToken ?? null,
      authorizedFetch,
      clearRejectedSession: () => clearSession('rejected'),
      expiresAt: session?.expiresAt ?? null,
      isAuthenticated: session !== null,
      login,
      logout,
      sessionEndReason,
    }),
    [authorizedFetch, clearSession, login, logout, session, sessionEndReason],
  )

  return <AdminAuthContext value={value}>{children}</AdminAuthContext>
}
