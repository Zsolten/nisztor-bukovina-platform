import { env } from 'node:process'

export default {
  async fetch(request: Request) {
    const backendUrl = env.BACKEND_URL

    if (!backendUrl) {
      return Response.json(
        { error: 'BACKEND_URL is not configured' },
        { status: 500 },
      )
    }

    const incomingUrl = new URL(request.url)
    const path = incomingUrl.searchParams.get('path')

    if (!path || path.split('/').includes('..')) {
      return Response.json(
        { error: 'Invalid API path' },
        { status: 400 },
      )
    }

    incomingUrl.searchParams.delete('path')

    const baseUrl = backendUrl.replace(/\/+$/, '')

    const targetUrl = new URL(`${baseUrl}/api/${path}`)
    targetUrl.search = incomingUrl.searchParams.toString()

    console.log('Proxy request:', {
      method: request.method,
      target: targetUrl.toString(),
    })

    try {
      const response = await fetch(targetUrl, {
        method: request.method,
        headers: {
          Accept: 'application/json',
        },
        signal: AbortSignal.timeout(15_000),
      })

      console.log('Backend response:', {
        status: response.status,
        target: targetUrl.toString(),
      })

      const body = await response.arrayBuffer()

      return new Response(body, {
        status: response.status,
        headers: {
          'Content-Type':
            response.headers.get('content-type') ?? 'application/json',
        },
      })
    } catch (error) {
      console.error('Backend proxy failed:', error)

      return Response.json(
        {
          error: 'Backend request failed',
          message:
            error instanceof Error
              ? error.message
              : String(error),
          target: targetUrl.toString(),
        },
        { status: 502 },
      )
    }
  },
}