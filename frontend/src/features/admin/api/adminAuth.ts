export interface AdminLoginResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresAt: string
}

interface AdminAuthenticationError {
  code?: string
}

export class AdminAuthApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    public readonly retryAfterSeconds: number | null = null,
  ) {
    super(`Administrator authentication failed with status ${status}`)
  }
}

function retryAfterSeconds(response: Response) {
  const value = Number(response.headers.get('Retry-After'))
  return Number.isFinite(value) && value > 0 ? Math.ceil(value) : null
}

async function authenticationError(response: Response) {
  let body: AdminAuthenticationError = {}

  try {
    body = (await response.json()) as AdminAuthenticationError
  } catch {
    // The generic fallback below intentionally does not expose backend details.
  }

  return new AdminAuthApiError(
    response.status,
    body.code ?? 'ADMIN_AUTHENTICATION_FAILED',
    retryAfterSeconds(response),
  )
}

export async function loginAdmin(email: string, password: string): Promise<AdminLoginResponse> {
  const response = await fetch('/api/admin/auth/login', {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  })

  if (!response.ok) {
    throw await authenticationError(response)
  }

  return (await response.json()) as AdminLoginResponse
}

export async function logoutAdmin(accessToken: string) {
  const response = await fetch('/api/admin/auth/logout', {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      Authorization: `Bearer ${accessToken}`,
    },
  })

  if (!response.ok) {
    throw await authenticationError(response)
  }
}
