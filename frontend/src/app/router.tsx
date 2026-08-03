import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Navigate, redirect, type RouteObject, useParams } from 'react-router-dom'
import {
  DEFAULT_LANGUAGE,
  isSupportedLanguage,
  PREFERRED_LANGUAGE_KEY,
  readPreferredLanguage,
} from '../i18n/languages'
import FoundationScreen from './FoundationScreen'

function LanguageRoute() {
  const { lang } = useParams()
  const { i18n, t } = useTranslation()

  useEffect(() => {
    if (!isSupportedLanguage(lang)) {
      return
    }

    localStorage.setItem(PREFERRED_LANGUAGE_KEY, lang)
    void i18n.changeLanguage(lang)
  }, [i18n, lang])

  if (!isSupportedLanguage(lang)) {
    return <Navigate to={`/${DEFAULT_LANGUAGE}`} replace />
  }

  return <FoundationScreen title={t('app.title')} />
}

export const appRoutes: RouteObject[] = [
  {
    path: '/',
    loader: () => redirect(`/${readPreferredLanguage()}`),
  },
  {
    path: '/:lang',
    element: <LanguageRoute />,
  },
  {
    path: '*',
    element: <Navigate to={`/${DEFAULT_LANGUAGE}`} replace />,
  },
]
