export const supportedLanguages = ['hu', 'ro', 'en'] as const

export type SupportedLanguage = (typeof supportedLanguages)[number]

export const defaultLanguage: SupportedLanguage = 'hu'

export function isSupportedLanguage(value: string | undefined): value is SupportedLanguage {
  return supportedLanguages.some((language) => language === value)
}
