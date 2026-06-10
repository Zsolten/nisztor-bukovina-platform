import { useParams } from 'react-router-dom'
import { defaultLanguage, isSupportedLanguage, type SupportedLanguage } from '../i18n/languages'

export function useActiveLanguage(): SupportedLanguage {
  const { lang } = useParams()
  return isSupportedLanguage(lang) ? lang : defaultLanguage
}
