import { useTranslation } from 'react-i18next'
import { NavLink, Outlet } from 'react-router-dom'
import { LanguageSwitcher } from '../components/LanguageSwitcher'
import { localizedPath } from '../routing/localizedPath'
import { useActiveLanguage } from '../routing/useActiveLanguage'

const adminNavItems = [
  { labelKey: 'admin.dashboard', path: 'admin' },
  { labelKey: 'admin.bookings', path: 'admin/foglalasok' },
  { labelKey: 'admin.content', path: 'admin/tartalom' },
  { labelKey: 'admin.login', path: 'admin/login' },
] as const

export function AdminLayout() {
  const { t } = useTranslation()
  const language = useActiveLanguage()

  return (
    <div className="admin-shell">
      <header className="admin-topbar">
        <NavLink to={localizedPath(language, 'admin')} className="brand">
          <span>{t('admin.title')}</span>
          <span>{t('app.brand')}</span>
        </NavLink>
        <nav className="admin-nav" aria-label="Admin navigation">
          {adminNavItems.map((item) => (
            <NavLink key={item.path} to={localizedPath(language, item.path)} end={item.path === 'admin'}>
              {t(item.labelKey)}
            </NavLink>
          ))}
        </nav>
        <LanguageSwitcher />
      </header>
      <main className="admin-page">
        <Outlet />
      </main>
    </div>
  )
}
