export type ContentLanguage = 'hu' | 'ro' | 'en'

export interface AdminGuesthouseTranslation {
  language: ContentLanguage
  version: number | null
  name: string
  shortDescription: string
  description: string
  roomDescription: string
  historyTitle: string
  historyText: string
}

export interface AdminGuesthouseContent {
  id: string
  slug: string
  active: boolean
  translations: AdminGuesthouseTranslation[]
}

export type ContentField =
  'name' | 'shortDescription' | 'description' | 'roomDescription' | 'historyTitle' | 'historyText'

export class AdminGuesthouseContentApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code?: string,
    public readonly fieldErrors: Partial<Record<ContentField, 'REQUIRED' | 'TOO_LONG'>> = {},
    public readonly currentContent: AdminGuesthouseTranslation | null = null,
  ) {
    super('Admin guesthouse content operation failed')
  }
}

type AuthorizedFetch = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>

export async function fetchAdminGuesthouseContent(
  authorizedFetch: AuthorizedFetch,
  signal?: AbortSignal,
) {
  const response = await authorizedFetch('/api/admin/guesthouses/content', {
    headers: { Accept: 'application/json' },
    signal,
  })
  if (!response.ok) throw await contentApiError(response)
  return (await response.json()) as AdminGuesthouseContent[]
}

export async function updateAdminGuesthouseTranslation(
  authorizedFetch: AuthorizedFetch,
  guesthouseId: string,
  translation: AdminGuesthouseTranslation,
) {
  const { language, ...content } = translation
  const response = await authorizedFetch(
    `/api/admin/guesthouses/${encodeURIComponent(guesthouseId)}/translations/${language}`,
    {
      method: 'PUT',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(content),
    },
  )
  if (!response.ok) throw await contentApiError(response)
  return (await response.json()) as AdminGuesthouseTranslation
}

async function contentApiError(response: Response) {
  try {
    const body = (await response.json()) as {
      code?: string
      fieldErrors?: Partial<Record<ContentField, 'REQUIRED' | 'TOO_LONG'>>
      currentContent?: AdminGuesthouseTranslation | null
    }
    return new AdminGuesthouseContentApiError(
      response.status,
      body.code,
      body.fieldErrors,
      body.currentContent,
    )
  } catch {
    return new AdminGuesthouseContentApiError(response.status)
  }
}
