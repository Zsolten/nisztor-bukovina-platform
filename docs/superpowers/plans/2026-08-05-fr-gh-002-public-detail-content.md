# FR-GH-002 Public Guesthouse Detail Content Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publish the approved three-language guesthouse history, rooms, amenities, prices, contact details, address, and localized gallery metadata in a comfortable responsive detail page without adding booking.

**Architecture:** The YAML remains an editorial source and an additive `V2` Flyway migration seeds normalized PostgreSQL tables. Module-local read services expose persistence-independent views to `GuesthouseQueryService`, which composes the additive public API; focused React presentation components consume only that API and reuse React-Bootstrap plus the current Sass tokens.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring Data JPA/JdbcClient, PostgreSQL 16, Flyway, JUnit 5, MockMvc, Testcontainers, ArchUnit, OpenAPI 3.1, React 19.2.6, TypeScript 6.0.2, React-Bootstrap 2.10.10, Bootstrap 5.3.8, Sass 1.102.0, Vitest 4.1.10, React Testing Library 16.3.2.

## Global Constraints

- Do not modify `backend/src/main/resources/db/migration/V1__create_guesthouses.sql`; all schema and seed changes belong to additive `V2__expand_guesthouse_public_details.sql`.
- `docs/content/guesthouses.hu-ro-en.yaml` is an editorial/seed source and must never be loaded by the browser or backend at runtime.
- Supported languages are exactly `hu`, `ro`, and `en`; missing localized database text falls back to Hungarian.
- Preserve the package-by-feature boundaries; no module may consume another module's DAO or JPA entity.
- No booking CTA, booking request, guest-data collection, availability, room allocation, calculation, admin UI/API, or embedded map is added.
- Prices remain the user-approved YAML amounts in `RON`; the page displays them but performs no calculation.
- Use the existing React-Bootstrap/Sass stack; add no UI, icon, state-management, or YAML runtime dependency.
- Remove the body radial gradient and repeating circular stripe overlay completely; add no replacement texture, pattern, decorative SVG, or CSS illustration.
- Use layered `paper-light` sheets with deterministic offsets; `forest-dark` is limited to the hero title card and contact sheet.
- The gallery preserves DOM/focus order, uses deterministic CSS Grid rather than CSS Columns or dense/random placement, and stays two-column at 320px except for full-width anchor images.
- Interactive targets remain at least 44×44 CSS pixels, visible focus remains, and reduced-motion behavior is preserved.
- Preserve unrelated working-tree changes, especially the existing `backend/gradlew` mode change.

## Target File Map

### Create

- `backend/src/main/resources/db/migration/V2__expand_guesthouse_public_details.sql`: normalized schema and approved HU/RO/EN seed.
- `backend/src/test/java/com/bukovina/platform/accommodation/guesthouse/GuesthouseContentMigrationTests.java`: migration/seed invariants.
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dao/GuesthouseContentQueryDao.java`: localized image/contact/address queries owned by guesthouse.
- `backend/src/main/java/com/bukovina/platform/accommodation/roomtype/service/RoomTypeQuery.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/roomtype/service/RoomTypeView.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/roomtype/service/RoomTypeQueryService.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/roomtype/dao/RoomTypeQueryDao.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/amenity/service/AmenityQuery.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/amenity/service/AmenityView.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/amenity/service/AmenityQueryService.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/amenity/dao/AmenityQueryDao.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/pricing/service/PricingQuery.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/pricing/service/PricingView.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/pricing/service/PricingQueryService.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/pricing/dao/PricingQueryDao.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/GuesthouseHistoryResponse.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/GuesthouseContactResponse.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/GuesthouseAddressResponse.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/RoomTypeResponse.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/AmenityResponse.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/PriceItemResponse.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/PricingAdjustmentResponse.java`
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/GuesthousePricingResponse.java`
- `frontend/src/features/accommodation/GuesthouseQuickFacts.tsx`
- `frontend/src/features/accommodation/GuesthouseStory.tsx`
- `frontend/src/features/accommodation/GuesthouseRoomTypes.tsx`
- `frontend/src/features/accommodation/GuesthouseAmenities.tsx`
- `frontend/src/features/accommodation/GuesthousePricing.tsx`
- `frontend/src/features/accommodation/GuesthouseContact.tsx`
- `frontend/src/features/accommodation/GuesthouseDetailPage.test.tsx`
- `docs/features/FR-GH-002-public-detail-content.md`

### Modify

- `docs/content/guesthouses.hu-ro-en.yaml`: record the user's price approval.
- `docs/api/openapi.yaml`: additive detail response contract.
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/model/GuesthouseTranslation.java`: history fields.
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/dto/GuesthouseDetailResponse.java`: additive response fields.
- `backend/src/main/java/com/bukovina/platform/accommodation/guesthouse/service/GuesthouseQueryService.java`: compose module query views.
- `backend/src/test/java/com/bukovina/platform/accommodation/guesthouse/controller/PublicGuesthouseControllerTests.java`: localized detail and inactive-content coverage.
- `backend/src/test/java/com/bukovina/platform/architecture/ModuleArchitectureTests.java`: prevent cross-module DAO/entity coupling.
- `frontend/src/shared/api/guesthouses.ts`: exact response interfaces.
- `frontend/src/i18n/resources.ts`: section, unit, contact, and accessibility labels.
- `frontend/src/app/router.test.tsx`: complete detail fixture and route regression.
- `frontend/src/features/accommodation/GuesthouseDetailPage.tsx`: section orchestration.
- `frontend/src/features/accommodation/GuesthouseGallery.tsx`: stable positional class names.
- `frontend/src/features/accommodation/GuesthouseGallery.test.tsx`: DOM order and selected-image regression.
- `frontend/src/shared/styles/_global.scss`: clean background, layered sheets, components, and responsive gallery.
- `docs/architecture/data-model.md`: implemented V2 tables.
- `docs/traceability/requirements-traceability.md`: partial FR-GH-002 status.
- `docs/README.md`: feature-document link.

---

### Task 1: Approve the editorial price source and define the additive API contract

**Files:**
- Modify: `docs/content/guesthouses.hu-ro-en.yaml`
- Modify: `docs/api/openapi.yaml`

**Interfaces:**
- Consumes: approved YAML structures `shared.contact`, `shared.address`, `shared.services`, `shared.pricing`, and each guesthouse's `content`, `history`, `rooms`, and `media.gallery`.
- Produces: `GuesthouseDetail` fields `history`, `contacts`, `address`, `roomTypes`, `amenities`, and `pricing` with the exact shapes used by backend and frontend tasks.

- [ ] **Step 1: Run the price-approval assertion and verify the current failure**

```bash
ruby -ryaml -e 'd=YAML.load_file("docs/content/guesthouses.hu-ro-en.yaml"); p=d.dig("shared","pricing"); abort "prices not approved" unless p["verification"]["status"] == "verified" && p["publication_requires_owner_confirmation"] == false'
```

Expected: exit 1 with `prices not approved`.

- [ ] **Step 2: Record the 2026-08-05 user approval without changing amounts**

Add a `direct_confirmation` source named `owner-price-confirmation`, set `shared.pricing.verification.status` to `verified`, include that source ID, set `publication_requires_owner_confirmation: false`, and remove `current_prices` from `owner_confirmation_required`. Keep the nine amounts `130, 200, 45, 75, 75, 175, 250, 325, 600`, the 1% tourist tax, the 10% coach discount, the 25% under-10 discount, and all payment notes unchanged.

- [ ] **Step 3: Extend the OpenAPI detail schema**

Define these required response shapes:

```yaml
GuesthouseHistory:
  required: [title, text]
GuesthouseContact:
  required: [type, value, label, preferred]
GuesthouseAddress:
  required: [formatted, latitude, longitude]
RoomType:
  required: [id, name, quantity, standardOccupancy, roomsWithExtraBed, extraBedsPerEligibleRoom, features]
Amenity:
  required: [id, name, category]
PriceItem:
  required: [id, label, amount, unit]
PricingAdjustment:
  required: [id, label, percentage]
GuesthousePricing:
  required: [currency, items, surcharges, discounts, paymentNote]
```

`Amenity.description` is optional. `GuesthouseDetail` requires `history`, `contacts`, `address`, `roomTypes`, `amenities`, and `pricing` in addition to existing fields. Price units are `person_night`, `person`, and `day`; amenity categories are `ROOM_COMFORT`, `FOOD_KITCHEN`, `OUTDOOR_WELLNESS`, and `PROGRAM_GROUP`.

- [ ] **Step 4: Validate YAML syntax and approval invariants**

Run the Step 1 assertion again, then:

```bash
ruby -ryaml -e 'd=YAML.load_file("docs/content/guesthouses.hu-ro-en.yaml"); abort unless d.dig("shared","pricing","items").length == 9; abort unless d["guesthouses"].all? { |g| %w[hu ro en].all? { |l| g.dig("content","name",l) } }'
```

Expected: both commands exit 0.

- [ ] **Step 5: Commit the approved source and contract**

```bash
git add docs/content/guesthouses.hu-ro-en.yaml docs/api/openapi.yaml
git commit -m "feat(FR-GH-002): approve detail content contract"
```

---

### Task 2: Add and verify the normalized V2 schema and seed

**Files:**
- Create: `backend/src/test/java/com/bukovina/platform/accommodation/guesthouse/GuesthouseContentMigrationTests.java`
- Create: `backend/src/main/resources/db/migration/V2__expand_guesthouse_public_details.sql`

**Interfaces:**
- Consumes: the exact approved YAML content from Task 1 and the two UUIDs/slugs created by V1.
- Produces: normalized localized tables queryable by `guesthouse_id`, `language_code`, `active`, and `display_order`.

- [ ] **Step 1: Write the failing migration integration test**

Create a Spring Boot/Testcontainers test using `JdbcTemplate` and assert:

```java
assertEquals(6, count("guesthouse_translation"));
assertEquals(60 * 3, count("guesthouse_image_translation"));
assertEquals(6, count("room_type"));
assertEquals(23, count("amenity"));
assertEquals(46, count("guesthouse_amenity"));
assertEquals(18, count("price_item"));
assertEquals(6, count("pricing_adjustment"));
assertEquals(12, count("guesthouse_contact"));
assertEquals(2, count("guesthouse_address"));
```

Also assert `bicycle_rental` and `domestic_animals` are absent, Nisztor has quantities `3/1/1`, Bukovina has `6/5/1`, and English image 1 has the YAML English alt text.

Use this test helper only with the fixed table-name literals above:

```java
private int count(String tableName) {
  return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
}
```

- [ ] **Step 2: Run the test and verify it fails before V2 exists**

```bash
cd backend
./gradlew test --tests '*GuesthouseContentMigrationTests'
```

Expected: FAIL because V2 tables are absent.

- [ ] **Step 3: Create the additive schema**

The migration must:

- add nullable `history_title` and `history_text` to `guesthouse_translation`, upsert all six HU/RO/EN translation rows, then make both columns non-null;
- create `guesthouse_image_translation(image_id, language_code, alt_text)` with a composite primary key;
- create guesthouse-owned contact plus localized contact-label tables and address plus localized formatted-address tables;
- create `room_type` plus `room_type_translation`;
- create `amenity`, `amenity_translation`, and `guesthouse_amenity`;
- create `guesthouse_pricing`, `guesthouse_pricing_translation`, `price_item`, `price_item_translation`, `pricing_adjustment`, and `pricing_adjustment_translation`;
- constrain language codes to `hu|ro|en`, categories and units to the OpenAPI enums, percentages to `0..100`, quantities/amounts to non-negative values, and display orders to non-negative values;
- seed both guesthouses independently even where current contact, address, amenity, and pricing values match.

Map the 23 public amenities exactly as follows:

```text
ROOM_COMFORT: wifi, private_bathroom, television, central_heating, air_conditioning,
              refrigerator, iron, drying_rack, hairdryer
FOOD_KITCHEN: floor_kitchens, sandwich_maker, dining_air_conditioning, dining_room,
              homemade_meals, meal_plans
OUTDOOR_WELLNESS: private_parking, outdoor_cooking, terrace, playground,
                  sun_loungers, wellness_tub
PROGRAM_GROUP: games, programme_planning
```

- [ ] **Step 4: Run migration and full backend checks**

```bash
./gradlew test --tests '*GuesthouseContentMigrationTests'
./gradlew spotlessCheck checkstyleMain checkstyleTest
```

Expected: migration assertions and formatting/static checks pass.

- [ ] **Step 5: Commit the schema and seed**

```bash
git add backend/src/main/resources/db/migration/V2__expand_guesthouse_public_details.sql backend/src/test/java/com/bukovina/platform/accommodation/guesthouse/GuesthouseContentMigrationTests.java
git commit -m "feat(FR-GH-002): seed normalized guesthouse details"
```

---

### Task 3: Expose module query contracts and compose the backend detail response

**Files:**
- Create/modify: all backend Java files listed in the target map for guesthouse detail DTOs/DAO, roomtype, amenity, and pricing.
- Modify: `PublicGuesthouseControllerTests.java`
- Modify: `ModuleArchitectureTests.java`

**Interfaces:**
- Produces these stable persistence-independent contracts:

```java
public interface RoomTypeQuery {
  List<RoomTypeView> findPublished(UUID guesthouseId, String language);
}
public interface AmenityQuery {
  List<AmenityView> findPublished(UUID guesthouseId, String language);
}
public interface PricingQuery {
  PricingView findPublished(UUID guesthouseId, String language);
}
```

`RoomTypeView` contains `id`, `name`, `quantity`, `standardOccupancy`, `roomsWithExtraBed`, `extraBedsPerEligibleRoom`, and feature IDs. `AmenityView` contains `id`, `name`, nullable `description`, `category`, and `displayOrder`. `PricingView` contains currency, ordered items, surcharge adjustments, discount adjustments, and payment note.

Use these pricing record signatures so the DAO, service, mapper, and response stay consistent:

```java
public record PricingView(
    String currency,
    List<PricingItemView> items,
    List<PricingAdjustmentView> surcharges,
    List<PricingAdjustmentView> discounts,
    String paymentNote) {}
public record PricingItemView(String id, String label, BigDecimal amount, String unit) {}
public record PricingAdjustmentView(String id, String label, BigDecimal percentage) {}
```

`GuesthouseContentQueryDao` exposes `findImages(UUID, String)`, `findContacts(UUID, String)`, and `findAddress(UUID, String)`. Contact types are exactly `PERSON`, `PHONE`, and `EMAIL`.

- [ ] **Step 1: Extend controller tests first**

For `/api/guesthouses/nisztor-panzio?lang=en`, assert:

```java
.andExpect(jsonPath("$.name").value("Nisztor Guesthouse"))
.andExpect(jsonPath("$.history.title").value("Bukovina Szekler heritage in Cristur"))
.andExpect(jsonPath("$.roomTypes.length()").value(3))
.andExpect(jsonPath("$.roomTypes[0].quantity").value(3))
.andExpect(jsonPath("$.amenities.length()").value(23))
.andExpect(jsonPath("$.pricing.currency").value("RON"))
.andExpect(jsonPath("$.pricing.items[0].amount").value(130))
.andExpect(jsonPath("$.address.formatted").value("17 Bucovina Street, Cristur 330003, Hunedoara County, Romania"))
.andExpect(jsonPath("$.images[0].altText").value("Group of five people in traditional clothing in front of Nisztor Guesthouse"));
```

Add a transactional test that deactivates one amenity, room type, and price item and proves each disappears. Keep the inactive-guesthouse and invalid-language tests. Update the Hungarian-fallback test to delete the requested English `guesthouse_translation` and one English `guesthouse_image_translation` inside the test transaction before asserting Hungarian fallback; V2 now seeds all three languages by default.

- [ ] **Step 2: Run the controller test and verify missing fields fail**

```bash
./gradlew test --tests '*PublicGuesthouseControllerTests'
```

Expected: FAIL because the additive response has not been composed.

- [ ] **Step 3: Implement localized read DAOs and module services**

Use `JdbcClient` in each module DAO. Join the requested translation and Hungarian translation, then select localized text with `COALESCE(requested.field, hu.field)`. Filter assignments and content by `active = TRUE`; order by persisted `display_order`. Services are read-only and expose only their view records, never rows/entities.

- [ ] **Step 4: Compose guesthouse detail DTOs**

Extend `GuesthouseTranslation` with `historyTitle/historyText`. Inject `GuesthouseContentQueryDao`, `RoomTypeQuery`, `AmenityQuery`, and `PricingQuery` into `GuesthouseQueryService`. Map module views into the response records and build:

```java
public record GuesthouseDetailResponse(
    String slug,
    String name,
    String shortDescription,
    int roomCount,
    GuesthouseImageResponse coverImage,
    String description,
    String roomDescription,
    List<GuesthouseImageResponse> images,
    GuesthouseHistoryResponse history,
    List<GuesthouseContactResponse> contacts,
    GuesthouseAddressResponse address,
    List<RoomTypeResponse> roomTypes,
    List<AmenityResponse> amenities,
    GuesthousePricingResponse pricing) {}
```

The list endpoint remains summary-only. Detail images and cover image use localized image translations with Hungarian fallback.

- [ ] **Step 5: Add architecture protection**

Add ArchUnit rules preventing classes outside each module from depending on `..roomtype.dao..`, `..amenity.dao..`, `..pricing.dao..`, or any of their future `..model..` packages. Query interfaces and view records in `..service..` remain allowed.

- [ ] **Step 6: Run focused and full backend checks**

```bash
./gradlew test --tests '*PublicGuesthouseControllerTests' --tests '*ModuleArchitectureTests'
./gradlew check
```

Expected: all detail, inactive-content, fallback, migration, architecture, format, and static checks pass.

- [ ] **Step 7: Commit the backend composition**

```bash
git add backend/src/main/java backend/src/test/java
git commit -m "feat(FR-GH-002): expose complete guesthouse details"
```

---

### Task 4: Add the typed frontend contract and translations

**Files:**
- Modify: `frontend/src/shared/api/guesthouses.ts`
- Modify: `frontend/src/i18n/resources.ts`
- Modify: `frontend/src/app/router.test.tsx`

**Interfaces:**
- Consumes: exact OpenAPI field names from Task 1.
- Produces: TypeScript `GuesthouseHistory`, `GuesthouseContact`, `GuesthouseAddress`, `GuesthouseRoomType`, `GuesthouseAmenity`, `GuesthousePriceItem`, `GuesthousePricingAdjustment`, and `GuesthousePricing` interfaces used by presentation components.

- [ ] **Step 1: Extend TypeScript types and the route fixture**

Use string unions:

```ts
export type AmenityCategory =
  | 'ROOM_COMFORT'
  | 'FOOD_KITCHEN'
  | 'OUTDOOR_WELLNESS'
  | 'PROGRAM_GROUP'

export type PriceUnit = 'person_night' | 'person' | 'day'
```

Extend `nisztorDetail` with one representative item for every new section so existing route tests continue to model the real contract.

- [ ] **Step 2: Add complete localized UI labels**

Add HU/RO/EN labels for: `quickFacts`, `location`, `privateBathroom`, `history`, `roomTypes`, `quantity`, `capacity`, `extraBed`, `amenities`, all four category names, `pricing`, all three price units, `touristTax`, `discounts`, `contact`, `preferredPhone`, `email`, `address`, and `openMap`. Do not duplicate API-owned amenity, room, price, or history copy in i18n resources.

- [ ] **Step 3: Run existing frontend tests with the complete fixture**

```bash
cd frontend
npm run test -- src/app/router.test.tsx
```

Expected: PASS; the fixture matches the new contract while the existing page remains compatible.

- [ ] **Step 4: Commit the typed contract and translations**

```bash
git add frontend/src/shared/api/guesthouses.ts frontend/src/i18n/resources.ts frontend/src/app/router.test.tsx
git commit -m "feat(FR-GH-002): define frontend detail contract"
```

---

### Task 5: Implement focused information components and the layered detail page

**Files:**
- Create: `GuesthouseQuickFacts.tsx`, `GuesthouseStory.tsx`, `GuesthouseRoomTypes.tsx`, `GuesthouseAmenities.tsx`, `GuesthousePricing.tsx`, `GuesthouseContact.tsx`
- Create: `GuesthouseDetailPage.test.tsx`
- Modify: `GuesthouseDetailPage.tsx`
- Modify: `_global.scss`

**Interfaces:**
- Consumes: localized data-only props from Task 4; components perform no fetch and know no YAML structure.
- Produces: ordered semantic sections with classes `detail-sheet`, `detail-sheet--left`, `detail-sheet--right`, and `detail-sheet--dark`.

- [ ] **Step 1: Write the failing detail-page behavior test**

Mock a complete HU response and assert visible headings/content for history, room types, grouped service tags, `130 RON`, `1%`, address, clickable `tel:+40743677812`, clickable `mailto:nisztorpanzio@gmail.com`, and no element whose accessible name matches `/foglal|book|rezerv/i`.

- [ ] **Step 2: Run the focused test and verify it fails**

```bash
npm run test -- src/features/accommodation/GuesthouseDetailPage.test.tsx
```

Expected: FAIL because the new sections do not exist.

- [ ] **Step 3: Implement the six presentation components**

Use React-Bootstrap `Badge`, `Card`, `Row`, `Col`, and responsive table/list primitives where they improve semantics. Tags are `Badge as="span"`, with no click handler, pointer cursor, hover state, or button role. Group amenities with a fixed category-order array rather than object iteration.

Format contact links without spaces in the URI while preserving readable text:

```ts
const phoneHref = `tel:${contact.value.replace(/\s+/g, '')}`
```

Build the map link from coordinates with `https://www.google.com/maps/search/?api=1&query=<lat>,<lng>` and use `target="_blank" rel="noreferrer"`.

- [ ] **Step 4: Assemble the page in the approved order**

Keep loading/error/back/hero behavior, then render:

```tsx
<GuesthouseQuickFacts guesthouse={data} />
<GuesthouseGallery images={data.images} />
<GuesthouseStory description={data.description} history={data.history} />
<GuesthouseRoomTypes roomTypes={data.roomTypes} />
<GuesthouseAmenities amenities={data.amenities} />
<GuesthousePricing pricing={data.pricing} />
<GuesthouseContact contacts={data.contacts} address={data.address} />
```

Hide an optional section when its list is empty. Keep the main `h1`, use section `h2` headings and card/group `h3` headings.

- [ ] **Step 5: Implement the layered sheet system and clean background**

Replace the body rules with:

```scss
body {
  margin: 0;
  min-width: 320px;
  min-height: 100vh;
  background: var(--paper);
}
```

Delete `body::before`. Add offset light sheets with borders, restrained shadow, and alternating margins; at `max-width: 620px`, reduce offsets to at most `0.5rem` and allow full-width sheets. Only `.detail-title-card` and `.contact-sheet` use `forest-dark`; pricing stays light.

- [ ] **Step 6: Run the detail test and accessibility regressions**

```bash
npm run test -- src/features/accommodation/GuesthouseDetailPage.test.tsx src/app/router.test.tsx
npm run lint
```

Expected: all new content is visible, links are correct, no booking control exists, route/language behavior passes.

- [ ] **Step 7: Commit the information UI**

```bash
git add frontend/src/features/accommodation frontend/src/shared/styles/_global.scss
git commit -m "feat(FR-GH-002): present complete guesthouse information"
```

---

### Task 6: Implement the deterministic staggered gallery

**Files:**
- Modify: `GuesthouseGallery.tsx`
- Modify: `GuesthouseGallery.test.tsx`
- Modify: `_global.scss`

**Interfaces:**
- Consumes: the ordered `GuesthouseImage[]` from the API.
- Produces: the same ordered image buttons and unchanged Modal behavior with deterministic classes `gallery-item--pattern-0` through `gallery-item--pattern-5`.

- [ ] **Step 1: Add the failing order/class regression test**

Render eight images and assert button accessible names remain in input order, every button has `gallery-item`, class patterns repeat after six items, clicking item 7 opens image 7, and next/previous navigation remains cyclic.

- [ ] **Step 2: Run the gallery test and verify the class assertion fails**

```bash
npm run test -- src/features/accommodation/GuesthouseGallery.test.tsx
```

- [ ] **Step 3: Add stable positional classes without changing data order**

```tsx
className={`gallery-item gallery-item--pattern-${index % 6}`}
```

Do not sort, splice, group, or duplicate `images`.

- [ ] **Step 4: Replace the matrix CSS**

Desktop uses 12 columns and six documented span/aspect/offset patterns; do not use `grid-auto-flow: dense`. Tablet uses two balanced columns. At `max-width: 620px`, non-anchor images span six columns, patterns 0 and 5 span all 12 columns, and alternate half-width patterns receive a small top margin. Keep at least `0.5rem` gaps and preserve the full button surface.

- [ ] **Step 5: Run focused and complete frontend verification**

```bash
npm run test -- src/features/accommodation/GuesthouseGallery.test.tsx
npm run format:check
npm run lint
npm run test
npm run build
```

Expected: all tests and production build pass; Modal keyboard/backdrop behavior remains.

- [ ] **Step 6: Commit the gallery redesign**

```bash
git add frontend/src/features/accommodation/GuesthouseGallery.tsx frontend/src/features/accommodation/GuesthouseGallery.test.tsx frontend/src/shared/styles/_global.scss
git commit -m "feat(FR-GH-002): add staggered responsive gallery"
```

---

### Task 7: Document partial FR-GH-002 completion and run final verification

**Files:**
- Create: `docs/features/FR-GH-002-public-detail-content.md`
- Modify: `docs/architecture/data-model.md`
- Modify: `docs/traceability/requirements-traceability.md`
- Modify: `docs/README.md`

**Interfaces:**
- Consumes: verified migration/API/UI behavior from Tasks 1–6.
- Produces: acceptance mapping that marks the public information slice implemented while booking/admin acceptance remains in progress.

- [ ] **Step 1: Write the feature evidence document**

Map each delivered requirement to schema/API/frontend evidence and automated tests. State explicitly that booking navigation, booking submission, admin editing, and immediate admin-to-public propagation are not delivered by this slice.

- [ ] **Step 2: Update architecture and traceability**

Replace the `roomtype`, `pricing`, and `amenity` entries under “planned” with the implemented V2 tables/query contracts. Add `FR-GH-002` as `Részben megvalósítva` in traceability and link the feature document. Add the feature document to `docs/README.md`.

- [ ] **Step 3: Run content and forbidden-pattern checks**

```bash
ruby -ryaml -e 'YAML.load_file("docs/content/guesthouses.hu-ro-en.yaml")'
rg "repeating-radial-gradient|body::before|radial-gradient" frontend/src/shared/styles
rg -i "booking|foglal|rezerv" frontend/src/features/accommodation/GuesthouseDetailPage.tsx frontend/src/features/accommodation/Guesthouse*.tsx
```

Expected: YAML parses; both `rg` commands return no matches in the scoped implementation files.

- [ ] **Step 4: Run complete project checks**

Backend:

```bash
cd backend
./gradlew check
```

Frontend:

```bash
cd frontend
npm run format:check
npm run lint
npm run test
npm run build
```

Expected: every command exits 0.

- [ ] **Step 5: Perform responsive visual verification**

Inspect both detail routes in HU/RO/EN at 320, 768, and 1280 CSS pixels. Confirm no horizontal page scroll, no background pattern, safe sheet offsets, readable long Romanian text, non-interactive wrapping tags, clear prices, 44px contact targets, three-column stagger on desktop, two-column alternating gallery on phone, full-width anchor images, and correct lightbox order.

- [ ] **Step 6: Commit documentation and any verification fixes**

```bash
git add docs/features/FR-GH-002-public-detail-content.md docs/architecture/data-model.md docs/traceability/requirements-traceability.md docs/README.md
git commit -m "docs(FR-GH-002): record public detail evidence"
```

Do not stage `backend/gradlew` unless the user separately requests its mode change.
