import type { TranslationResources } from './resources'

declare module 'i18next' {
  interface CustomTypeOptions {
    defaultNS: 'translation'
    resources: TranslationResources
    enableSelector: false
  }
}
