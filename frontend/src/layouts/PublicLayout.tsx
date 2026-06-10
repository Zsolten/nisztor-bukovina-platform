import { useTranslation } from 'react-i18next'
import { NavLink, Outlet } from 'react-router-dom'
import { LanguageSwitcher } from '../components/LanguageSwitcher'
import { localizedPath } from '../routing/localizedPath'
import { useActiveLanguage } from '../routing/useActiveLanguage'

const navItems = [
  { labelKey: 'nav.home', path: '' },
  { labelKey: 'nav.guesthouses', path: 'panzioink' },
  { labelKey: 'nav.rooms', path: 'szobak' },
  { labelKey: 'nav.attractions', path: 'latnivalok' },
  { labelKey: 'nav.dayTrips', path: 'csillagturak' },
  { labelKey: 'nav.booking', path: 'foglalasi-keres' },
  { labelKey: 'nav.itinerary', path: 'programajanlo' },
] as const

export function PublicLayout() {
  const { t } = useTranslation()
  const language = useActiveLanguage()

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink to={localizedPath(language)} className="brand">
          <span>{t('app.brand')}</span>
          <span>{t('app.subtitle')}</span>
        </NavLink>
        <nav className="main-nav" aria-label="Main navigation">
          {navItems.map((item) => (
            <NavLink key={item.path} to={localizedPath(language, item.path)} end={item.path === ''}>
              {t(item.labelKey)}
            </NavLink>
          ))}
        </nav>
        <LanguageSwitcher />
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
