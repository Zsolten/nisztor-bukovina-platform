import type { SupportedLanguage } from '../i18n/languages'

export function localizedPath(language: SupportedLanguage, path = '') {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (normalizedPath === '/') {
    return `/${language}`
  }

  return `/${language}${normalizedPath}`
}
