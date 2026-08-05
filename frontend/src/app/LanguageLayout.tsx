import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, Navigate, Outlet, useLocation, useParams } from 'react-router-dom'
import {
  DEFAULT_LANGUAGE,
  isSupportedLanguage,
  PREFERRED_LANGUAGE_KEY,
  SUPPORTED_LANGUAGES,
  type Language,
} from '../i18n/languages'

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
      <header className="site-header">
        <Link className="brand" to={`/${lang}`} aria-label={t('app.navigation.home')}>
          <span className="brand-mark" aria-hidden="true">
            NB
          </span>
          <span>
            <strong>{t('app.title')}</strong>
            <small>{t('app.location')}</small>
          </span>
        </Link>

        <nav className="language-switcher" aria-label={t('app.navigation.languages')}>
          {SUPPORTED_LANGUAGES.map((language) => (
            <Link
              key={language}
              className={language === lang ? 'active' : undefined}
              to={languagePath(location.pathname, language)}
              aria-current={language === lang ? 'page' : undefined}
            >
              {LANGUAGE_LABELS[language]}
            </Link>
          ))}
        </nav>
      </header>

      <Outlet context={{ language: lang }} />

      <footer className="site-footer">
        <p>{t('app.footer.message')}</p>
        <p>
          © {new Date().getFullYear()} {t('app.footer.copyright')}
        </p>
      </footer>
    </div>
  )
}

export interface LanguageOutletContext {
  language: Language
}
