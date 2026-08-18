import type { PublicAttraction } from '../../shared/api/tourism'

export type AttractionCategory = 'castle' | 'nature' | 'church' | 'museum' | 'other'

export function categoryForAttraction(attraction: Pick<PublicAttraction, 'slug' | 'name'>) {
  const value = `${attraction.slug} ${attraction.name}`.toLocaleLowerCase('hu')
  if (/(templom|székesegyház|katedrális|church)/.test(value)) return 'church'
  if (/(vár|kastély|citadella|erőd|vara|castel)/.test(value)) return 'castle'
  if (/(hegy|szurdok|barlang|vízesés|fürdő|mountain|cave)/.test(value)) return 'nature'
  if (/(múzeum|tájház|museum)/.test(value)) return 'museum'
  return 'other'
}
