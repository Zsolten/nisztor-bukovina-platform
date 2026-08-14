import type { Language } from '../../i18n/languages'

export interface GuesthouseImage {
  path: string
  altText: string
  cover: boolean
}

export interface GuesthouseSummary {
  id: string
  slug: string
  name: string
  shortDescription: string
  roomCount: number
  coverImage: GuesthouseImage
}

export interface GuesthouseHistory {
  title: string
  text: string
}

export interface GuesthousePageText {
  storyEyebrow: string
  storyTitle: string
  diningEyebrow: string
  diningTitle: string
  diningDescription: string
  amenitiesTitle: string
  roomTypesTitle: string
  pricingTitle: string
  historyEyebrow: string
  galleryTitle: string
  galleryHint: string
}

export type GuesthouseContactType = 'PERSON' | 'PHONE' | 'EMAIL'

export interface GuesthouseContact {
  type: GuesthouseContactType
  value: string
  label: string
  preferred: boolean
}

export interface GuesthouseAddress {
  formatted: string
  latitude: number
  longitude: number
}

export interface GuesthouseRoomType {
  id: string
  name: string
  quantity: number
  standardOccupancy: number
  roomsWithExtraBed: number
  extraBedsPerEligibleRoom: number
  features: string[]
}

export type AmenityCategory = 'ROOM_COMFORT' | 'FOOD_KITCHEN' | 'OUTDOOR_WELLNESS' | 'PROGRAM_GROUP'

export interface GuesthouseAmenity {
  id: string
  name: string
  description?: string
  detailedDescription?: string
  category: AmenityCategory
  pricingType: 'FREE' | 'PAID'
}

export type PriceUnit = 'person_night' | 'person' | 'day'

export interface GuesthousePriceItem {
  id: string
  label: string
  amount: number
  unit: PriceUnit
}

export interface GuesthousePricingAdjustment {
  id: string
  label: string
  percentage: number
}

export interface GuesthousePricing {
  currency: 'RON'
  items: GuesthousePriceItem[]
  taxes: GuesthousePricingAdjustment[]
  surcharges: GuesthousePricingAdjustment[]
  discounts: GuesthousePricingAdjustment[]
  paymentNote: string
}

export interface GuesthouseDetail extends GuesthouseSummary {
  description: string
  roomDescription: string
  pageText: GuesthousePageText
  images: GuesthouseImage[]
  history: GuesthouseHistory
  contacts: GuesthouseContact[]
  address: GuesthouseAddress
  roomTypes: GuesthouseRoomType[]
  amenities: GuesthouseAmenity[]
  pricing: GuesthousePricing
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
