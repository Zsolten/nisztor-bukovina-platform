import { useTranslation } from 'react-i18next'
import { NavLink, useLocation } from 'react-router-dom'
import { supportedLanguages, type SupportedLanguage } from '../i18n/languages'
import { useActiveLanguage } from '../routing/useActiveLanguage'

function languageTarget(pathname: string, activeLanguage: SupportedLanguage, targetLanguage: SupportedLanguage) {
  if (pathname.startsWith(`/${activeLanguage}`)) {
    return pathname.replace(`/${activeLanguage}`, `/${targetLanguage}`)
  }

  return `/${targetLanguage}`
}

export function LanguageSwitcher() {
  const { t } = useTranslation()
  const { pathname } = useLocation()
  const activeLanguage = useActiveLanguage()

  return (
    <div className="language-switcher" aria-label="Language">
      {supportedLanguages.map((language) => (
        <NavLink
          key={language}
          to={languageTarget(pathname, activeLanguage, language)}
          className={language === activeLanguage ? 'active' : undefined}
          aria-label={t('language.switchTo', { language: t(`language.${language}`) })}
        >
          {t(`language.${language}`)}
        </NavLink>
      ))}
    </div>
  )
}
