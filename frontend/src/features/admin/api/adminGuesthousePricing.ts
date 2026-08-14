export interface AdminPriceItem {
  code: string
  label: string
  amount: number
  unit: 'person_night' | 'room_night' | 'person' | 'day'
}

export interface AdminPricingAdjustment {
  code: string
  label: string
  percentage: number
}

export interface AdminGuesthousePricing {
  guesthouseId: string
  currency: 'RON'
  items: AdminPriceItem[]
  surcharges: AdminPricingAdjustment[]
  discounts: AdminPricingAdjustment[]
}

export interface AdminGuesthousePricingUpdate {
  items: Array<Pick<AdminPriceItem, 'code' | 'amount'>>
  surcharges: Array<Pick<AdminPricingAdjustment, 'code' | 'percentage'>>
  discounts: Array<Pick<AdminPricingAdjustment, 'code' | 'percentage'>>
}

export class AdminGuesthousePricingApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code?: string,
    public readonly fieldErrors: Record<string, string> = {},
  ) {
    super('Admin guesthouse pricing operation failed')
  }
}

type AuthorizedFetch = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>

export async function fetchAdminGuesthousePricing(
  authorizedFetch: AuthorizedFetch,
  guesthouseId: string,
  signal?: AbortSignal,
) {
  const response = await authorizedFetch(
    `/api/admin/guesthouses/${encodeURIComponent(guesthouseId)}/pricing`,
    { headers: { Accept: 'application/json' }, signal },
  )
  if (!response.ok) throw await pricingApiError(response)
  return (await response.json()) as AdminGuesthousePricing
}

export async function updateAdminGuesthousePricing(
  authorizedFetch: AuthorizedFetch,
  guesthouseId: string,
  pricing: AdminGuesthousePricingUpdate,
) {
  const response = await authorizedFetch(
    `/api/admin/guesthouses/${encodeURIComponent(guesthouseId)}/pricing`,
    {
      method: 'PUT',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(pricing),
    },
  )
  if (!response.ok) throw await pricingApiError(response)
  return (await response.json()) as AdminGuesthousePricing
}

async function pricingApiError(response: Response) {
  try {
    const body = (await response.json()) as { code?: string; fieldErrors?: Record<string, string> }
    return new AdminGuesthousePricingApiError(response.status, body.code, body.fieldErrors)
  } catch {
    return new AdminGuesthousePricingApiError(response.status)
  }
}
