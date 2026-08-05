# Homepage Content Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enrich the multilingual public homepage with family history, nearby destinations, a temporary review and a map while preserving the existing React-Bootstrap visual system.

**Architecture:** The homepage remains inside the accommodation feature and continues to fetch only guesthouse summaries from the backend. New editorial sections are static, translated React components composed by `GuesthouseListPage`; no new API contract or persistence model is introduced.

**Tech Stack:** React 19, TypeScript 6, React-Bootstrap 2, react-i18next 17, SCSS, Vitest and Testing Library.

## Global Constraints

- Keep the existing welcome heading and current guesthouse selection flow.
- Use only supplied local images and normalize new filenames to lowercase descriptive kebab-case.
- Preserve HU, RO and EN frontend translations.
- Do not add dependencies, backend endpoints, booking actions or persisted reviews.
- Preserve responsive layouts, 44 px interactive targets and reduced-motion behavior.

---

### Task 1: Normalize the supplied homepage assets

**Files:**
- Rename: `frontend/public/images/guesthouses/bukovina/about_us.jpg` to `frontend/public/images/homepage/family-hosts.jpg`
- Rename: `frontend/public/images/latnivalok/Cetatea_medievală_Deva,_în_lumina_răsăritului.jpg` to `frontend/public/images/destinations/deva-citadel.jpg`
- Rename: `frontend/public/images/latnivalok/szeben.jpg` to `frontend/public/images/destinations/sibiu-old-town.jpg`
- Rename: `frontend/public/images/latnivalok/nn_tordai-sobanya-24-q3.jpeg` to `frontend/public/images/destinations/turda-salt-mine.jpg`
- Rename: `frontend/public/images/latnivalok/retezat.jpg` to `frontend/public/images/destinations/retezat-mountains.jpg`
- Rename the new `_DSC*` guesthouse photographs sequentially to `nisztor-room-01.jpg` and `bukovina-detail-01.jpg` style names within their current guesthouse directories.

**Interfaces:**
- Produces: stable public paths consumed by homepage component constants and future gallery migrations.

- [ ] **Step 1: Record the exact source and target paths**

Verify each source exists with `Get-ChildItem frontend/public/images -Recurse -File` and ensure no target already exists.

- [ ] **Step 2: Rename only newly supplied assets**

Use PowerShell `Move-Item -LiteralPath` for each validated source-target pair. Do not restore or rename files already deleted by the user.

- [ ] **Step 3: Verify normalized naming**

Run:

```powershell
Get-ChildItem frontend/public/images -Recurse -File | Where-Object Name -Match '[_ÁÉÍÓÖŐÚÜŰ ]'
```

Expected: no newly supplied homepage, destination or `_DSC*` file appears.

### Task 2: Add failing homepage content tests

**Files:**
- Modify: `frontend/src/app/router.test.tsx`
- Modify: `frontend/src/features/accommodation/GuesthouseDetailPage.test.tsx` only if shared navigation assertions require adjustment

**Interfaces:**
- Consumes: the existing `appRoutes` and `AppProviders` test harness.
- Produces: assertions for translated legacy, surroundings, temporary review and map landmarks.

- [ ] **Step 1: Extend the Hungarian homepage test**

After the existing guesthouse assertions, require:

```tsx
expect(screen.getByRole('heading', { name: 'Közel 30 éve vendégségben' })).toBeVisible()
expect(screen.getByRole('heading', { name: 'Javasolt kirándulási irányok' })).toBeVisible()
expect(screen.getByText('Déva, Vajdahunyad, Gyulafehérvár')).toBeVisible()
expect(screen.getByRole('heading', { name: 'Vendégeink mondták' })).toBeVisible()
expect(screen.getByTitle('Csernakeresztúr térképe')).toBeVisible()
```

- [ ] **Step 2: Verify the new test fails**

Run:

```powershell
npm.cmd run test -- src/app/router.test.tsx
```

Expected: FAIL because the new sections do not exist.

### Task 3: Implement the editorial homepage sections

**Files:**
- Create: `frontend/src/features/accommodation/HomepageLegacy.tsx`
- Create: `frontend/src/features/accommodation/HomepageSurroundings.tsx`
- Create: `frontend/src/features/accommodation/HomepageReview.tsx`
- Create: `frontend/src/features/accommodation/HomepageMap.tsx`
- Modify: `frontend/src/features/accommodation/GuesthouseListPage.tsx`
- Modify: `frontend/src/i18n/resources.ts`

**Interfaces:**
- Each new component exports one default parameterless React component and reads its copy through `useTranslation()`.
- `HomepageSurroundings` owns a local array of four entries with `image`, `titleKey` and `descriptionKey` values.
- `GuesthouseListPage` renders the components after the guesthouse section in legacy, surroundings, review and map order.

- [ ] **Step 1: Add complete HU, RO and EN translation keys**

Create `homepage.legacy`, `homepage.surroundings`, `homepage.review` and `homepage.map` groups. The Hungarian legacy body must use the approved text verbatim except for corrected accents and punctuation. Translate equivalent meaning into Romanian and English.

- [ ] **Step 2: Implement the family legacy section**

Render `/images/homepage/family-hosts.jpg`, an eyebrow, `Közel 30 éve vendégségben` heading and translated body in a semantic `section` with a stable image aspect ratio.

- [ ] **Step 3: Implement four surroundings items**

Use the normalized Deva, Sibiu, Turda and Retezat images and the four supplied descriptions. Each item is a non-interactive `article`, not a fake link or card button.

- [ ] **Step 4: Implement temporary review and map**

Render one translated testimonial labelled as a guest opinion without claiming a specific external platform. Embed an OpenStreetMap-based iframe titled from `homepage.map.frameTitle`, plus an external OpenStreetMap link for Cristur.

- [ ] **Step 5: Compose the homepage**

Import and render all four components after the existing guesthouse `Container`, retaining the current hero and guesthouse API state handling.

- [ ] **Step 6: Run the focused test**

Run `npm.cmd run test -- src/app/router.test.tsx`.

Expected: PASS.

### Task 4: Refine navigation, typography and responsive presentation

**Files:**
- Modify: `frontend/src/shared/styles/_global.scss`
- Modify: `frontend/src/app/LanguageLayout.tsx` only if a sticky-header accessibility attribute is required

**Interfaces:**
- Consumes: class names emitted by the four homepage components.
- Produces: sticky navigation and responsive editorial layouts without changing component behavior.

- [ ] **Step 1: Make the navbar sticky**

Set `.site-header` to `position: sticky; top: 0`, add an opaque or lightly translucent paper background and sufficient z-index. Keep the existing centered maximum width and offcanvas behavior.

- [ ] **Step 2: Reduce oversized typography**

Reduce the desktop hero maximum from `8.7rem` to approximately `7.4rem` and section-heading maximum from `4.4rem` to approximately `3.8rem`. Keep fixed breakpoint ranges rather than viewport-proportional body text.

- [ ] **Step 3: Style the four new full-width sections**

Use unframed full-width bands, stable image aspect ratios, no nested cards and no decorative gradients. Add restrained image hover scaling and alternating content rhythm on desktop.

- [ ] **Step 4: Add responsive rules**

At existing tablet/mobile breakpoints, collapse legacy and map grids, surroundings from two to one column, and ensure headings and map iframe cannot overflow.

- [ ] **Step 5: Preserve reduced motion**

Extend the existing `prefers-reduced-motion` rule so new image transitions and reveal animations are disabled.

### Task 5: Keep persisted gallery paths aligned with normalized assets

**Files:**
- Create: `backend/src/main/resources/db/migration/V3__refresh_guesthouse_galleries.sql`
- Modify: `backend/src/test/java/com/bukovina/platform/accommodation/guesthouse/GuesthouseContentMigrationTests.java`
- Modify: `backend/src/test/java/com/bukovina/platform/accommodation/guesthouse/controller/PublicGuesthouseControllerTests.java`

**Interfaces:**
- Preserves the existing guesthouse API and database schema while removing image rows whose files were intentionally replaced.
- Produces gallery paths and translated alt text for the normalized Nisztor and Bukovina assets.

- [x] **Step 1: Add failing migration and API assertions**

Require 10 Nisztor gallery images and 16 Bukovina gallery images with cover-specific translated alt text.

- [x] **Step 2: Add the Flyway data migration**

Delete obsolete gallery rows, retain paths backed by actual files and update their HU, RO and EN alt text.

- [x] **Step 3: Verify with PostgreSQL**

Run the focused Testcontainers tests, then `gradlew.bat clean check`.

Expected: Flyway applies all migrations and the full backend check exits 0.

### Task 6: Verify and visually inspect

**Files:**
- Modify only files needed to correct defects discovered during verification.

**Interfaces:**
- Produces: a buildable, tested homepage at desktop and mobile widths.

- [ ] **Step 1: Format and lint**

Run:

```powershell
npm.cmd exec -- prettier --write src/app/LanguageLayout.tsx src/app/router.test.tsx src/features/accommodation src/i18n/resources.ts src/shared/styles/_global.scss
npm.cmd run lint
```

Expected: both commands exit 0.

- [ ] **Step 2: Run all frontend tests**

Run `npm.cmd run test`.

Expected: all test files pass.

- [ ] **Step 3: Build production assets**

Run `npm.cmd run build`.

Expected: TypeScript and Vite build exit 0.

- [ ] **Step 4: Start the development server**

Run `npm.cmd run dev -- --host 127.0.0.1` and retain the reported URL.

- [ ] **Step 5: Inspect desktop and mobile layouts**

Capture the Hungarian homepage at approximately 1440x900 and 390x844. Verify sticky navigation, non-overlapping text, legible map, complete section order, correct images and no horizontal overflow.
