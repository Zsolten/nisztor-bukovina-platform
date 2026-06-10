export const guesthouses = [
  {
    slug: 'nisztor',
    name: 'Nisztor panzió',
    locationKey: 'guesthouses.nisztor.location',
    descriptionKey: 'guesthouses.nisztor.description',
  },
  {
    slug: 'bukovina',
    name: 'Bukovina panzió',
    locationKey: 'guesthouses.bukovina.location',
    descriptionKey: 'guesthouses.bukovina.description',
  },
] as const

export const rooms = [
  {
    slug: 'nisztor-family',
    propertySlug: 'nisztor',
    propertyName: 'Nisztor panzió',
    nameKey: 'rooms.items.family.name',
    descriptionKey: 'rooms.items.family.description',
    capacity: 4,
  },
  {
    slug: 'bukovina-double',
    propertySlug: 'bukovina',
    propertyName: 'Bukovina panzió',
    nameKey: 'rooms.items.double.name',
    descriptionKey: 'rooms.items.double.description',
    capacity: 2,
  },
] as const

export const attractionCards = [
  {
    slug: 'curated',
    titleKey: 'tourism.items.curated.title',
    descriptionKey: 'tourism.items.curated.description',
    categoryKey: 'tourism.categories.curated',
  },
  {
    slug: 'practical',
    titleKey: 'tourism.items.practical.title',
    descriptionKey: 'tourism.items.practical.description',
    categoryKey: 'tourism.categories.access',
  },
  {
    slug: 'routes',
    titleKey: 'tourism.items.routes.title',
    descriptionKey: 'tourism.items.routes.description',
    categoryKey: 'tourism.categories.seasonal',
  },
] as const

export const dayTripCards = [
  {
    slug: 'half-day',
    titleKey: 'dayTrips.items.halfDay.title',
    descriptionKey: 'dayTrips.items.halfDay.description',
    hours: 4,
  },
  {
    slug: 'full-day',
    titleKey: 'dayTrips.items.fullDay.title',
    descriptionKey: 'dayTrips.items.fullDay.description',
    hours: 8,
  },
] as const
