import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import { defaultLanguage, supportedLanguages } from './languages'
import { resources } from './resources'

i18n.use(initReactI18next).init({
  resources,
  lng: defaultLanguage,
  fallbackLng: defaultLanguage,
  supportedLngs: [...supportedLanguages],
  interpolation: {
    escapeValue: false,
  },
})

export default i18n
