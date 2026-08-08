export default {
  async fetch(request: Request) {
    const backendUrl = process.env.BACKEND_URL

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

    // "path" is only an internal Vercel routing parameter.
    // Don't send it to Spring Boot.
    incomingUrl.searchParams.delete('path')

    const baseUrl = backendUrl.replace(/\/+$/, '')
    const targetUrl = new URL(`${baseUrl}/api/${path}`)

    targetUrl.search = incomingUrl.searchParams.toString()

    const headers = new Headers(request.headers)

    // Let fetch generate these for the actual backend request.
    headers.delete('host')
    headers.delete('content-length')

    // This is server-to-server, so browser CORS headers aren't needed.
    headers.delete('origin')

    const hasBody = request.method !== 'GET' && request.method !== 'HEAD'

    const response = await fetch(targetUrl, {
      method: request.method,
      headers,
      body: hasBody ? await request.arrayBuffer() : undefined,
      redirect: 'manual',
    })

    const responseHeaders = new Headers(response.headers)

    // Vercel/fetch should calculate these for the returned response.
    responseHeaders.delete('content-length')

    return new Response(response.body, {
      status: response.status,
      headers: responseHeaders,
    })
  },
}