import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import { DEFAULT_LANGUAGE, SUPPORTED_LANGUAGES } from './languages'
import { resources } from './resources'

void i18n.use(initReactI18next).init({
  resources,
  lng: DEFAULT_LANGUAGE,
  fallbackLng: DEFAULT_LANGUAGE,
  defaultNS: 'translation',
  supportedLngs: SUPPORTED_LANGUAGES,
  initAsync: false,
  interpolation: { escapeValue: false },
})

export default i18n
