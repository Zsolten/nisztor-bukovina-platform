import { useEffect, useState } from 'react'
import type { Language } from '../../i18n/languages'
import {
  fetchGuesthouse,
  fetchGuesthouses,
  type GuesthouseDetail,
  type GuesthouseSummary,
} from '../../shared/api/guesthouses'

interface QueryState<T> {
  data: T | null
  loading: boolean
  error: boolean
}

interface SettledQuery<T> {
  data: T | null
  error: boolean
  requestKey: string | null
}

const INITIAL_QUERY = { data: null, error: false, requestKey: null }

function visibleState<T>(state: SettledQuery<T>, requestKey: string | null): QueryState<T> {
  if (requestKey === null) return { data: null, loading: false, error: false }

  const isCurrent = state.requestKey === requestKey
  return {
    data: isCurrent ? state.data : null,
    loading: !isCurrent,
    error: isCurrent && state.error,
  }
}

export function useGuesthouses(language: Language): QueryState<GuesthouseSummary[]> {
  const [state, setState] = useState<SettledQuery<GuesthouseSummary[]>>(INITIAL_QUERY)

  useEffect(() => {
    const controller = new AbortController()

    void fetchGuesthouses(language, controller.signal)
      .then((data) => setState({ data, error: false, requestKey: language }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setState({ data: null, error: true, requestKey: language })
      })

    return () => controller.abort()
  }, [language])

  return visibleState(state, language)
}

export function useGuesthouse(
  slug: string | null,
  language: Language,
): QueryState<GuesthouseDetail> {
  const requestKey = slug ? `${language}:${slug}` : null
  const [state, setState] = useState<SettledQuery<GuesthouseDetail>>(INITIAL_QUERY)

  useEffect(() => {
    if (!slug || !requestKey) return

    const controller = new AbortController()

    void fetchGuesthouse(slug, language, controller.signal)
      .then((data) => setState({ data, error: false, requestKey }))
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return
        setState({ data: null, error: true, requestKey })
      })

    return () => controller.abort()
  }, [language, requestKey, slug])

  return visibleState(state, requestKey)
}
