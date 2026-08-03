export const resources = {
  hu: {
    translation: {
      app: { title: 'Nisztor-Bukovina Platform' },
    },
  },
  ro: {
    translation: {
      app: { title: 'Nisztor-Bukovina Platform' },
    },
  },
  en: {
    translation: {
      app: { title: 'Nisztor-Bukovina Platform' },
    },
  },
} as const

export type TranslationResources = (typeof resources)['hu']['translation']
