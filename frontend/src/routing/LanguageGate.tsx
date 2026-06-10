import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Navigate, Outlet, useParams } from 'react-router-dom'
import { defaultLanguage, isSupportedLanguage } from '../i18n/languages'

export function LanguageGate() {
  const { lang } = useParams()
  const { i18n } = useTranslation()
  const validLanguage = isSupportedLanguage(lang)

  useEffect(() => {
    if (validLanguage && i18n.language !== lang) {
      void i18n.changeLanguage(lang)
    }
  }, [i18n, lang, validLanguage])

  if (!validLanguage) {
    return <Navigate to={`/${defaultLanguage}`} replace />
  }

  return <Outlet />
}
