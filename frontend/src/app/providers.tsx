import type { ReactNode } from 'react'
import { I18nextProvider } from 'react-i18next'
import { AdminAuthProvider } from '../features/admin/auth/AdminAuthProvider'
import i18n from '../i18n/config'

interface AppProvidersProps {
  children: ReactNode
}

export function AppProviders({ children }: AppProvidersProps) {
  return (
    <I18nextProvider i18n={i18n}>
      <AdminAuthProvider>{children}</AdminAuthProvider>
    </I18nextProvider>
  )
}
