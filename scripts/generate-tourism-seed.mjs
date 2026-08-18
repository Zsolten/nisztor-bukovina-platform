import { createHash } from 'node:crypto'
import { readFileSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const sourcePath = join(
  root,
  'backend',
  'src',
  'main',
  'resources',
  'db',
  'seed',
  'tourism-attractions.hu.json',
)
const outputPath = join(
  root,
  'backend',
  'src',
  'main',
  'resources',
  'db',
  'migration',
  'V24__seed_hungarian_attractions.sql',
)

const data = JSON.parse(readFileSync(sourcePath, 'utf8'))

function uuid(namespace, value) {
  const hex = createHash('sha256').update(`${namespace}:${value}`).digest('hex').slice(0, 32)
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-5${hex.slice(13, 16)}-a${hex.slice(17, 20)}-${hex.slice(20)}`
}

function sql(value) {
  if (value === null || value === undefined || value === '') return 'NULL'
  return `'${String(value).replaceAll("'", "''")}'`
}

function slug(value) {
  return value
    .normalize('NFD')
    .replaceAll(/\p{Diacritic}/gu, '')
    .toLowerCase()
    .replaceAll(/[^a-z0-9]+/g, '-')
    .replaceAll(/^-|-$/g, '')
}

const lines = [
  '-- Generated from db/seed/tourism-attractions.hu.json.',
  '-- Regenerate with: node scripts/generate-tourism-seed.mjs',
  '',
]

for (const collection of data.collections) {
  const id = uuid('tourism-collection', collection.key)
  const hu = collection.translations.hu
  lines.push(
    `INSERT INTO tourism_collection (id, slug, display_order, active) VALUES ('${id}', ${sql(collection.key)}, ${collection.sort_order}, ${collection.active !== false});`,
    `INSERT INTO tourism_collection_translation (collection_id, language_code, name, short_description) VALUES ('${id}', 'hu', ${sql(hu.name)}, ${sql(hu.short_description)});`,
  )
}

lines.push('')

for (const attraction of data.attractions) {
  const id = uuid('attraction', attraction.external_id ?? attraction.slug)
  const attractionSlug = slug(attraction.slug)
  const hu = attraction.translations.hu
  // The imported catalogue is usable immediately. Editorial activation can be changed in admin.
  lines.push(
    `INSERT INTO attraction (id, slug, latitude, longitude, google_maps_url, active) VALUES ('${id}', ${sql(attractionSlug)}, ${attraction.coordinates.latitude}, ${attraction.coordinates.longitude}, ${sql(attraction.google_maps_link)}, TRUE);`,
    `INSERT INTO attraction_translation (attraction_id, language_code, name, short_description, detailed_description, admission_information, practical_information) VALUES ('${id}', 'hu', ${sql(hu.title)}, ${sql(hu.short_description)}, ${sql(hu.detailed_description)}, ${sql(hu.admission_information)}, ${sql(hu.practical_information)});`,
  )
  for (const [index, collectionKey] of attraction.collection_keys.entries()) {
    lines.push(
      `INSERT INTO attraction_collection (attraction_id, collection_id, display_order) VALUES ('${id}', '${uuid('tourism-collection', collectionKey)}', ${attraction.sort_order ?? index});`,
    )
  }
}

writeFileSync(outputPath, `${lines.join('\n')}\n`, 'utf8')
console.log(`Generated ${outputPath} with ${data.collections.length} collections and ${data.attractions.length} attractions.`)
