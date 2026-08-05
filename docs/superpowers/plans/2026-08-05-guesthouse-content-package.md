# Multilingual Guesthouse Content Package Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create one human-readable and machine-readable YAML source of truth containing the complete public detail-page content for Nisztor Panzió and Bukovina Panzió in Hungarian, Romanian, and English.

**Architecture:** Store language-independent values once under `shared` or the relevant guesthouse, and store every visitor-facing string under fixed `hu`, `ro`, and `en` keys. Preserve provenance and freshness explicitly so later AI development can distinguish confirmed facts, editorial derivations, and facts that require owner confirmation.

**Tech Stack:** YAML 1.2-compatible data, repository Markdown documentation, Ruby Psych for syntax checks, shell and Ruby validation commands.

## Global Constraints

- Create exactly one deliverable: `docs/content/guesthouses.hu-ro-en.yaml`.
- Do not modify the application, database, API, frontend, or existing image files.
- Supported languages are exactly `hu`, `ro`, and `en`; the default language is `hu`.
- Store language-independent values only once.
- Use `verified`, `derived`, and `needs_owner_confirmation` as the only verification states.
- Do not present the Bukovina Szekler community history as a verified history of the Nisztor family.
- Mark every public price for owner confirmation before publication because prices are time-sensitive.
- Treat booking as an inquiry, not as an instant or guaranteed reservation.
- Do not store any guest personal data in the content package.
- Reference only image files that already exist below `frontend/public/images/guesthouses/nisztor` and `frontend/public/images/guesthouses/bukovina`.
- Research date is `2026-08-05`.

---

### Task 1: Create and verify the multilingual content source

**Files:**
- Create: `docs/content/guesthouses.hu-ro-en.yaml`
- Read: `docs/superpowers/specs/2026-08-05-guesthouse-content-package-design.md`
- Read: `backend/src/main/resources/db/migration/V1__create_guesthouses.sql`
- Read: `frontend/public/images/guesthouses/nisztor/gallery-01.jpg` through `gallery-26.jpg`
- Read: `frontend/public/images/guesthouses/bukovina/gallery-01.jpg` through `gallery-34.jpg`

**Interfaces:**
- Consumes: the approved design specification, current seed content, the existing local galleries, and the source pages listed below.
- Produces: one YAML document with top-level keys `schema_version`, `document`, `sources`, `shared`, `guesthouses`, and `owner_confirmation_required`.

**Authoritative source map:**

| Source ID | URL | Use |
| --- | --- | --- |
| `nisztor-home-hu` | `https://www.nisztorpanzio.ro/` | Hungarian positioning, hospitality, meals, booking form |
| `nisztor-about-hu` | `https://www.nisztorpanzio.ro/panzionkrol` | room counts, equipment, services |
| `nisztor-prices-hu` | `https://www.nisztorpanzio.ro/araink` | public prices, discounts, tax, payment note |
| `nisztor-contact-hu` | `https://www.nisztorpanzio.ro/elerhetoseg` | contacts, postal address, GPS coordinates |
| `nisztor-home-ro` | `https://www.pensiuneanisztor.ro/` | Romanian terminology and positioning |
| `cserna-about-hu` | `https://www.csernakereszturivendeghazak.ro/hu` | community heritage and rural tourism services |
| `cserna-about-en` | `https://www.csernakereszturivendeghazak.ro/en/about-us` | English community-history terminology |
| `project-seed` | repository file `backend/src/main/resources/db/migration/V1__create_guesthouses.sql` | approved names, slugs, descriptions, room counts and gallery paths |

- [ ] **Step 1: Create the YAML document skeleton and provenance section**

Create `docs/content/guesthouses.hu-ro-en.yaml` with this exact top-level order:

```yaml
schema_version: 1

document:
  title:
    hu: Nisztor és Bukovina Panzió — háromnyelvű tartalomforrás
    ro: Pensiunile Nisztor și Bukovina — sursă de conținut trilingvă
    en: Nisztor and Bukovina Guesthouses — trilingual content source
  purpose:
    hu: A két panzió publikus részletes oldalának ember és fejlesztő AI számára olvasható tartalmi forrása.
    ro: Sursa de conținut, lizibilă pentru oameni și sisteme AI de dezvoltare, pentru paginile publice de detaliu ale celor două pensiuni.
    en: The human-readable and developer-AI-readable content source for the public detail pages of both guesthouses.
  last_researched_at: 2026-08-05
  default_language: hu
  supported_languages: [hu, ro, en]

sources: []
shared: {}
guesthouses: []
owner_confirmation_required: []
```

Populate `sources` with the eight source IDs and locations from the authoritative source map. Each web source must have `accessed_at: 2026-08-05`; the repository source must have `revision: 6fbb9e6` and `path: backend/src/main/resources/db/migration/V1__create_guesthouses.sql`.

- [ ] **Step 2: Add shared contact and location data**

Under `shared.contact`, record:

- contact persons: `Nisztor István` and `Nisztor Éva`;
- landline/fax: `+40 254 236 172`;
- mobile numbers: `+40 743 677 812` and `+40 744 198 744`;
- email: `nisztorpanzio@gmail.com`;
- preferred public information number: `+40 743 677 812`.

Under `shared.address`, record the postal code `330003`, Romanian locality `Cristur`, Hungarian locality `Csernakeresztúr`, street `Strada Bucovina`, number `17`, county code `HD`, county name in all three languages, country code `RO`, country name in all three languages, latitude `45.82361`, and longitude `22.93869`.

Set the contact and location verification state to `verified`, cite `nisztor-contact-hu`, and include a trilingual note that these details currently apply to both guesthouses based on the owner's confirmation on `2026-08-05`.

- [ ] **Step 3: Add shared services**

Create stable service IDs and trilingual labels/descriptions for:

- private enclosed parking;
- Wi-Fi internet;
- private bathroom for every room;
- television in every room;
- shared refrigerator;
- microwave oven;
- toaster;
- sandwich maker;
- iron and ironing board;
- clothes drying rack;
- hairdryer;
- air conditioning in the dining area;
- barbecue, cauldron cooking and outdoor oven;
- terrace;
- dining rooms with capacities of 14 and 50 people;
- playground;
- sun loungers;
- board and card games;
- homemade Transylvanian-Hungarian meals using predominantly home-produced ingredients;
- breakfast, half-board and full-board meal options;
- multi-day itinerary assistance and local programme organisation;
- bicycle rental;
- opportunities to see domestic animals.

Use `availability: shared` for every service. Cite `nisztor-home-hu`, `nisztor-about-hu`, and `cserna-about-hu` as applicable. Mark bicycle rental and domestic-animal viewing as `needs_owner_confirmation` because they appear on the network site but not on the Nisztor site; mark the other directly supported items `verified`.

- [ ] **Step 4: Add the public price list as structured amounts**

Set `shared.pricing.currency: RON`, `shared.pricing.price_basis: per_person`, and `shared.pricing.verification.status: needs_owner_confirmation`.

Create these price items with numeric `amount` values and trilingual names:

| ID | Amount | Unit |
| --- | ---: | --- |
| `accommodation` | 130 | `person_night` |
| `single_occupancy_room` | 200 | `person_night` |
| `breakfast` | 45 | `person` |
| `lunch` | 75 | `person` |
| `dinner` | 75 | `person` |
| `bed_and_breakfast` | 175 | `person_night` |
| `half_board` | 250 | `person_night` |
| `full_board` | 325 | `person_night` |
| `tour_guide` | 600 | `day` |

Repeat `currency: RON` on every price item so each item remains self-describing when extracted independently. Add the `1%` tourist tax as a separate percentage surcharge. Add a `10%` coach-group discount and a `25%` discount for children under 10 as separate rules. Add a trilingual payment note stating that HUF and EUR may be accepted after conversion at the daily exchange rate. Cite `nisztor-prices-hu` and explicitly record `source_accessed_at: 2026-08-05` plus `publication_requires_owner_confirmation: true`.

- [ ] **Step 5: Add the booking-inquiry definition**

Set `shared.booking_request.action: inquiry` and add a trilingual explanation that submission starts a request and does not confirm availability or a reservation.

Add these fields in order, each with a stable `id`, HTML-neutral `type`, `required` boolean, and trilingual `label`:

| ID | Type | Required |
| --- | --- | --- |
| `guest_name` | `text` | true |
| `email` | `email` | true |
| `phone` | `tel` | false |
| `guesthouse` | `guesthouse_reference` | true |
| `arrival_date` | `date` | true |
| `departure_date` | `date` | true |
| `adult_count` | `integer` | true |
| `child_count` | `integer` | false |
| `requested_rooms` | `room_request` | false |
| `meal_plan` | `enum` | false |
| `message` | `multiline_text` | false |
| `privacy_consent` | `boolean` | true |

For `meal_plan`, list `none`, `breakfast`, `half_board`, and `full_board`. For the consent field, provide trilingual text without inventing a privacy-policy URL; set `privacy_policy_url.status: needs_owner_confirmation`.

- [ ] **Step 6: Add the Nisztor Panzió profile**

Create a guesthouse with `id: nisztor`, `slug: nisztor-panzio`, `room_count: 5`, and references to the shared contact, address, services, pricing, and booking request.

Write polished, meaning-preserving `hu`, `ro`, and `en` values for:

- `name`;
- `short_description`;
- `detailed_description` covering the quiet family atmosphere, location 200 metres from the village centre, position between Deva and Hunedoara, suitability for short stays and longer holidays, welcoming service, homemade food, and active programmes;
- `history.community_heritage` covering the Bukovina Szekler history from the 1764 Siculicidium through the five Bukovina villages, the 1911 resettlement, the 1915–1916 church, the 1920 Catholic school, and living dance traditions;
- `history.family_story.note` stating that a verified personal Nisztor family or business-founding narrative is not available in the current sources.

Mark `family_story` as `needs_owner_confirmation`; mark the community history `derived` and cite `cserna-about-hu` and `cserna-about-en`.

Add room types:

| ID | Quantity | Standard occupancy | Extra-bed capacity |
| --- | ---: | ---: | ---: |
| `double` | 3 | 2 | 1 on one of the three rooms |
| `triple` | 1 | 3 | 0 |
| `quadruple` | 1 | 4 | 0 |

Record private bathroom and television as features of every room. Express the extra-bed limitation structurally with `rooms_with_extra_bed: 1` and `extra_beds_per_eligible_room: 1`.

- [ ] **Step 7: Add the Bukovina Panzió profile**

Create a guesthouse with `id: bukovina`, `slug: bukovina-panzio`, `room_count: 12`, and references to the same shared content.

Write separate polished `hu`, `ro`, and `en` values for `name`, `short_description`, and `detailed_description`. The description may share verified hospitality facts with Nisztor but must not claim a distinct founding story or facility feature that the sources do not support.

Use the same split between `history.community_heritage` and `history.family_story`. Mark the Bukovina family/business story as `needs_owner_confirmation` and do not duplicate the community narrative as a claimed building history.

Add room types:

| ID | Quantity | Standard occupancy | Extra-bed capacity |
| --- | ---: | ---: | ---: |
| `double` | 6 | 2 | 1 on one of the six rooms |
| `triple` | 5 | 3 | 0 |
| `quadruple` | 1 | 4 | 0 |

Record private bathroom and television as features of every room. Express the extra-bed limitation with `rooms_with_extra_bed: 1` and `extra_beds_per_eligible_room: 1`.

- [ ] **Step 8: Add cover images and complete ordered galleries**

For Nisztor, set `/images/guesthouses/nisztor/gallery-01.jpg` as the cover and list all 26 image paths from `gallery-01.jpg` through `gallery-26.jpg` in numeric order.

For Bukovina, set `/images/guesthouses/bukovina/gallery-01.jpg` as the cover and list all 34 image paths from `gallery-01.jpg` through `gallery-34.jpg` in numeric order.

Each gallery item must contain:

- `id` such as `nisztor-gallery-01`;
- `path`;
- zero-based `display_order`;
- `cover` boolean;
- `alt` with `hu`, `ro`, and `en`;
- `alt_verification` with `verified` when the visible subject is clear or `needs_owner_confirmation` when it is not.

Inspect the local images before writing descriptive alt text. Describe only visible subjects and do not infer people, room type, ownership, season, or occasion without evidence.

- [ ] **Step 9: Add the owner-confirmation checklist**

Populate `owner_confirmation_required` with structured entries for:

- current validity and publication approval of all prices, discounts, tourist tax, and exchange-rate payment policy;
- whether every shared service currently applies to both buildings;
- current availability of bicycle rental and domestic-animal viewing;
- the personal Nisztor family and guesthouse-founding story;
- the personal Bukovina guesthouse-founding story;
- which image should be the preferred cover for each guesthouse;
- descriptive alt text for images whose subject is unclear;
- the privacy-policy URL used by the booking inquiry;
- preferred booking-request destination and response expectations.

Each entry must have `id`, `applies_to`, `question` in all three languages, `reason`, and `blocking_publication`.

- [ ] **Step 10: Validate syntax, completeness, paths, totals, and claims**

Run the YAML syntax check:

```bash
ruby -e 'require "yaml"; require "date"; YAML.safe_load_file("docs/content/guesthouses.hu-ro-en.yaml", permitted_classes: [Date], aliases: false); puts "YAML OK"'
```

Expected output: `YAML OK`.

Run this validation script inline; it exits non-zero unless:

- the three supported languages are exactly `hu`, `ro`, and `en`;
- there are exactly two guesthouses with IDs `nisztor` and `bukovina`;
- all trilingual objects contain non-empty `hu`, `ro`, and `en` values;
- Nisztor has 5 rooms across its room-type quantities and 26 gallery entries;
- Bukovina has 12 rooms across its room-type quantities and 34 gallery entries;
- exactly one cover image exists per guesthouse;
- every referenced image resolves below `frontend/public`;
- every price has numeric `amount`, `currency`, and `unit` information;
- every verification status is one of `verified`, `derived`, or `needs_owner_confirmation`.

```bash
ruby -ryaml -rdate - <<'RUBY'
path = "docs/content/guesthouses.hu-ro-en.yaml"
doc = YAML.safe_load_file(path, permitted_classes: [Date], aliases: false)
languages = %w[hu ro en]
allowed_statuses = %w[verified derived needs_owner_confirmation]
errors = []

unless doc.dig("document", "supported_languages") == languages
  errors << "supported_languages must be hu, ro, en in this order"
end

walk = lambda do |value, location|
  case value
  when Hash
    present_languages = value.keys & languages
    if present_languages.any?
      errors << "#{location} must contain hu, ro, en" unless present_languages.sort == languages.sort
      languages.each do |language|
        text = value[language]
        errors << "#{location}.#{language} must be non-empty" unless text.is_a?(String) && !text.strip.empty?
      end
    end
    if value.key?("status") && !allowed_statuses.include?(value["status"])
      errors << "#{location}.status is invalid: #{value['status'].inspect}"
    end
    value.each { |key, child| walk.call(child, "#{location}.#{key}") }
  when Array
    value.each_with_index { |child, index| walk.call(child, "#{location}[#{index}]") }
  end
end
walk.call(doc, "root")

guesthouses = doc.fetch("guesthouses")
unless guesthouses.map { |guesthouse| guesthouse["id"] } == %w[nisztor bukovina]
  errors << "guesthouse IDs or ordering are incorrect"
end

expected = {
  "nisztor" => { "rooms" => 5, "images" => 26 },
  "bukovina" => { "rooms" => 12, "images" => 34 }
}

guesthouses.each do |guesthouse|
  id = guesthouse.fetch("id")
  room_total = guesthouse.fetch("rooms").sum { |room| room.fetch("quantity") }
  errors << "#{id} room quantity total is #{room_total}" unless room_total == expected.fetch(id).fetch("rooms")

  gallery = guesthouse.dig("media", "gallery")
  errors << "#{id} gallery count is #{gallery.length}" unless gallery.length == expected.fetch(id).fetch("images")
  errors << "#{id} must have exactly one cover" unless gallery.count { |image| image["cover"] } == 1
  gallery.each do |image|
    local_path = File.join("frontend/public", image.fetch("path").sub(%r{\A/}, ""))
    errors << "missing image: #{local_path}" unless File.file?(local_path)
  end
end

doc.dig("shared", "pricing", "items").each do |item|
  errors << "#{item['id']} amount must be numeric" unless item["amount"].is_a?(Numeric)
  errors << "#{item['id']} currency must be RON" unless item["currency"] == "RON"
  errors << "#{item['id']} unit must be non-empty" unless item["unit"].is_a?(String) && !item["unit"].empty?
end

abort(errors.join("\n")) unless errors.empty?
puts "CONTENT VALIDATION OK"
RUBY
```

Expected output: `CONTENT VALIDATION OK`.

Run:

```bash
git diff --check
rg -n "TO[D]O|FIX[M]E|PLACE[H]OLDER|T[B]D" docs/content/guesthouses.hu-ro-en.yaml
git status --short
```

Expected: `git diff --check` succeeds; the placeholder scan returns no matches; Git lists only the intended content file and any plan-tracking change.

- [ ] **Step 11: Review the rendered content and commit**

Read the YAML from beginning to end and verify that:

- Hungarian copy is natural and hospitality-oriented;
- Romanian copy uses correct diacritics and natural pension terminology;
- English copy uses natural guesthouse and accommodation terminology;
- no translation changes a price, capacity, address, contact value, or historical date;
- statements that apply to both guesthouses do not appear as unsupported building-specific claims.

Commit the completed content package:

```bash
git add docs/content/guesthouses.hu-ro-en.yaml
git commit -m "docs: add multilingual guesthouse content source"
```
