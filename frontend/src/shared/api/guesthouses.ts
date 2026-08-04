import type { Language } from '../../i18n/languages'

export interface GuesthouseImage {
  path: string
  altText: string
  cover: boolean
}

export interface GuesthouseSummary {
  slug: string
  name: string
  shortDescription: string
  roomCount: number
  coverImage: GuesthouseImage
}

export interface GuesthouseDetail extends GuesthouseSummary {
  description: string
  roomDescription: string
  images: GuesthouseImage[]
}

async function getJson<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(path, {
    headers: { Accept: 'application/json' },
    signal,
  })

  if (!response.ok) {
    throw new Error(`Guesthouse API request failed with status ${response.status}`)
  }

  return (await response.json()) as T
}

export function fetchGuesthouses(language: Language, signal?: AbortSignal) {
  return getJson<GuesthouseSummary[]>(`/api/guesthouses?lang=${language}`, signal)
}

export function fetchGuesthouse(slug: string, language: Language, signal?: AbortSignal) {
  return getJson<GuesthouseDetail>(
    `/api/guesthouses/${encodeURIComponent(slug)}?lang=${language}`,
    signal,
  )
}
