import { useEffect, useState } from 'react'
import { Container, Nav, Navbar, Offcanvas } from 'react-bootstrap'
import { useTranslation } from 'react-i18next'
import { Link, Navigate, Outlet, useLocation, useParams } from 'react-router-dom'
import { ChevronDown } from 'lucide-react'
import {
  DEFAULT_LANGUAGE,
  isSupportedLanguage,
  PREFERRED_LANGUAGE_KEY,
  SUPPORTED_LANGUAGES,
  type Language,
} from '../i18n/languages'
import SiteFooter from '../shared/components/SiteFooter'

const LANGUAGE_LABELS: Record<Language, string> = {
  hu: 'HU',
  ro: 'RO',
  en: 'EN',
}

function languagePath(pathname: string, language: Language) {
  return pathname.replace(/^\/(hu|ro|en)(?=\/|$)/, `/${language}`)
}

export default function LanguageLayout() {
  const { lang } = useParams()
  const location = useLocation()
  const { i18n, t } = useTranslation()
  const [navigationOpen, setNavigationOpen] = useState(false)
  const [guesthouseMenuOpen, setGuesthouseMenuOpen] = useState(false)
  const [guesthouseMenuDismissed, setGuesthouseMenuDismissed] = useState(false)
  const [headerScrolled, setHeaderScrolled] = useState(() => window.scrollY > 24)
  const isHomepage =
    isSupportedLanguage(lang) &&
    (location.pathname === `/${lang}` || location.pathname === `/${lang}/`)
  const isTourismPage =
    isSupportedLanguage(lang) && location.pathname.startsWith(`/${lang}/star-tours`)
  const useLightHeader = isTourismPage ? false : !isHomepage || headerScrolled
  const closeNavigation = () => {
    setGuesthouseMenuOpen(false)
    setGuesthouseMenuDismissed(true)
    setNavigationOpen(false)
  }

  useEffect(() => {
    if (!isSupportedLanguage(lang)) return

    window.localStorage.setItem(PREFERRED_LANGUAGE_KEY, lang)
    document.documentElement.lang = lang
    void i18n.changeLanguage(lang)
  }, [i18n, lang])

  useEffect(() => {
    const updateHeaderState = () => setHeaderScrolled(window.scrollY > 24)

    updateHeaderState()
    window.addEventListener('scroll', updateHeaderState, { passive: true })

    return () => window.removeEventListener('scroll', updateHeaderState)
  }, [])

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }, [location.pathname])

  if (!isSupportedLanguage(lang)) {
    return <Navigate to={`/${DEFAULT_LANGUAGE}`} replace />
  }

  return (
    <div className="site-shell">
      <a className="skip-link" href="#main-content">
        {isTourismPage ? t('tourism.attractions') : t('app.navigation.guesthouses')}
      </a>
      <Navbar
        className={`site-header${useLightHeader ? ' site-header-scrolled' : ''}${isTourismPage ? ' site-header-tourism' : ''}`}
        expand="lg"
        expanded={navigationOpen}
        onToggle={setNavigationOpen}
      >
        <Container fluid>
          <Navbar.Brand
            as={Link}
            className="brand"
            to={`/${lang}`}
            aria-label={t('app.navigation.home')}
          >
            <span>
              <strong>{t('app.title')}</strong>
              <small>{t('app.location')}</small>
            </span>
          </Navbar.Brand>

          {isTourismPage && (
            <span className="tourism-current-language">{LANGUAGE_LABELS[lang]}</span>
          )}
          <Navbar.Toggle aria-controls="language-navigation" label={t('app.navigation.menu')} />
          <Navbar.Offcanvas
            id="language-navigation"
            aria-label={t('app.navigation.languages')}
            placement="end"
            responsive="lg"
            onHide={() => setNavigationOpen(false)}
          >
            <Offcanvas.Header closeButton closeLabel={t('app.navigation.closeMenu')}>
              <Offcanvas.Title>{t('app.navigation.languages')}</Offcanvas.Title>
            </Offcanvas.Header>
            <Offcanvas.Body>
              <Nav className="site-navigation ms-auto">
                <div
                  className={`guesthouse-navigation-dropdown${guesthouseMenuOpen ? ' is-open' : ''}${guesthouseMenuDismissed ? ' is-dismissed' : ''}`}
                  onMouseEnter={() => setGuesthouseMenuDismissed(false)}
                  onMouseLeave={() => setGuesthouseMenuOpen(false)}
                >
                  <Nav.Link
                    as={Link}
                    active={!isTourismPage}
                    to={`/${lang}`}
                    onClick={closeNavigation}
                  >
                    {t('app.navigation.guesthouses')}
                  </Nav.Link>
                  <button
                    className="guesthouse-navigation-toggle"
                    type="button"
                    aria-label={t('app.navigation.openGuesthouseMenu')}
                    aria-controls="guesthouse-navigation-menu"
                    aria-expanded={guesthouseMenuOpen}
                    onClick={() => {
                      setGuesthouseMenuDismissed(false)
                      setGuesthouseMenuOpen((open) => !open)
                    }}
                  >
                    <ChevronDown aria-hidden="true" size={15} strokeWidth={2} />
                  </button>
                  <div className="guesthouse-navigation-menu" id="guesthouse-navigation-menu">
                    <Link
                      to={`/${lang}/guesthouses/nisztor-panzio`}
                      onClick={closeNavigation}
                    >
                      {t('app.navigation.nisztorGuesthouse')}
                    </Link>
                    <Link
                      to={`/${lang}/guesthouses/bukovina-panzio`}
                      onClick={closeNavigation}
                    >
                      {t('app.navigation.bukovinaGuesthouse')}
                    </Link>
                    <Link
                      to={`/${lang}/booking`}
                      onClick={closeNavigation}
                    >
                      {t('app.navigation.booking')}
                    </Link>
                  </div>
                </div>
                <Nav.Link
                  as={Link}
                  active={isTourismPage}
                  to={`/${lang}/star-tours`}
                  onClick={closeNavigation}
                >
                  {t('app.navigation.starTours')}
                </Nav.Link>
              </Nav>
              <Nav className="language-switcher">
                {SUPPORTED_LANGUAGES.map((language) => (
                  <Nav.Link
                    as={Link}
                    key={language}
                    active={language === lang}
                    to={languagePath(location.pathname, language)}
                    aria-current={language === lang ? 'page' : undefined}
                    onClick={closeNavigation}
                  >
                    {LANGUAGE_LABELS[language]}
                  </Nav.Link>
                ))}
              </Nav>
            </Offcanvas.Body>
          </Navbar.Offcanvas>
        </Container>
      </Navbar>

      <Outlet context={{ language: lang }} />

      {!isTourismPage && <SiteFooter language={lang} />}
    </div>
  )
}

export interface LanguageOutletContext {
  language: Language
}
