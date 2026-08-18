import { Castle, Church, Landmark, Mountain, Waves } from 'lucide-react'
import type { AttractionCategory } from './tourismCategories'

export default function AttractionCategoryIcon({
  category,
  size = 18,
}: {
  category: AttractionCategory
  size?: number
}) {
  if (category === 'castle') return <Castle aria-hidden="true" size={size} strokeWidth={2.2} />
  if (category === 'church') return <Church aria-hidden="true" size={size} strokeWidth={2.2} />
  if (category === 'nature') return <Mountain aria-hidden="true" size={size} strokeWidth={2.2} />
  if (category === 'museum') return <Landmark aria-hidden="true" size={size} strokeWidth={2.2} />
  return <Waves aria-hidden="true" size={size} strokeWidth={2.2} />
}
