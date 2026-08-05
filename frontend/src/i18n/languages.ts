export const SUPPORTED_LANGUAGES = ['hu', 'ro', 'en'] as const
export type Language = (typeof SUPPORTED_LANGUAGES)[number]

export const DEFAULT_LANGUAGE: Language = 'hu'
export const PREFERRED_LANGUAGE_KEY = 'preferredLanguage'

export function isSupportedLanguage(value: unknown): value is Language {
  return typeof value === 'string' && (SUPPORTED_LANGUAGES as readonly string[]).includes(value)
}

export function readPreferredLanguage(): Language {
  const storedLanguage = window.localStorage.getItem(PREFERRED_LANGUAGE_KEY)
  return isSupportedLanguage(storedLanguage) ? storedLanguage : DEFAULT_LANGUAGE
}
