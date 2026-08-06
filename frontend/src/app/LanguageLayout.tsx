import { useEffect, useState } from 'react'
import { Container, Nav, Navbar, Offcanvas } from 'react-bootstrap'
import { useTranslation } from 'react-i18next'
import { Link, Navigate, Outlet, useLocation, useParams } from 'react-router-dom'
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

  useEffect(() => {
    if (!isSupportedLanguage(lang)) return

    window.localStorage.setItem(PREFERRED_LANGUAGE_KEY, lang)
    document.documentElement.lang = lang
    void i18n.changeLanguage(lang)
  }, [i18n, lang])

  if (!isSupportedLanguage(lang)) {
    return <Navigate to={`/${DEFAULT_LANGUAGE}`} replace />
  }

  return (
    <div className="site-shell">
      <a className="skip-link" href="#main-content">
        {t('app.navigation.guesthouses')}
      </a>
      <Navbar
        className="site-header"
        expand="md"
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
            <span className="brand-mark" aria-hidden="true">
              NB
            </span>
            <span>
              <strong>{t('app.title')}</strong>
              <small>{t('app.location')}</small>
            </span>
          </Navbar.Brand>

          <Navbar.Toggle aria-controls="language-navigation" label={t('app.navigation.menu')} />
          <Navbar.Offcanvas
            id="language-navigation"
            aria-label={t('app.navigation.languages')}
            placement="end"
            responsive="md"
            onHide={() => setNavigationOpen(false)}
          >
            <Offcanvas.Header closeButton closeLabel={t('app.navigation.closeMenu')}>
              <Offcanvas.Title>{t('app.navigation.languages')}</Offcanvas.Title>
            </Offcanvas.Header>
            <Offcanvas.Body>
              <Nav className="language-switcher ms-auto">
                {SUPPORTED_LANGUAGES.map((language) => (
                  <Nav.Link
                    as={Link}
                    key={language}
                    active={language === lang}
                    to={languagePath(location.pathname, language)}
                    aria-current={language === lang ? 'page' : undefined}
                    onClick={() => setNavigationOpen(false)}
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

      <SiteFooter language={lang} />
    </div>
  )
}

export interface LanguageOutletContext {
  language: Language
}
