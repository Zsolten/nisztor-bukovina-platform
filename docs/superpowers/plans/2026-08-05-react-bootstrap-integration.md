# React-Bootstrap Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Introduce a stable React-Bootstrap and Bootstrap 5 styling foundation, migrate the existing public guesthouse screens where it improves behavior, remove the striped background, and preserve the current visual identity with mobile-first behavior.

**Architecture:** Bootstrap Sass supplies the theme, grid, responsive utilities, and component styling. React-Bootstrap supplies React-native behavior for navigation, status feedback, layout, and the gallery modal; the existing feature-oriented React structure and the distinctive guesthouse visuals remain intact.

**Tech Stack:** React 19.2.6, TypeScript 6.0.2, Vite 8.0.12, React-Bootstrap 2.10.10, Bootstrap 5.3.8, Sass 1.102.0, Vitest 4.1.10, React Testing Library 16.3.2.

## Global Constraints

- Use Node.js 24.x and npm 11.x.
- Use stable `react-bootstrap@2.10.10`; do not use the `3.0.0-beta.5` prerelease.
- Do not import Bootstrap's JavaScript bundle or use a CDN.
- Preserve the existing paper, forest, gold, brick, typography, hero, card, and gallery identity.
- Remove the repeating linear striped/grid body background completely.
- Retain the subtle warm paper background and non-striped texture.
- Design from 320 pixels upward without horizontal page scrolling.
- Keep interactive targets at least 44 pixels high where they are used for primary navigation or actions.
- Keep the feature-oriented source structure and avoid wrappers around every React-Bootstrap component.
- Preserve localized accessible names, skip navigation, reduced-motion support, and keyboard operation.
- Do not add an admin UI, a new business feature, global state management, or an end-to-end framework in this change.
- After dependency changes, the npm audit result must not be worse than the baseline of three high-severity findings recorded before this work.

## Target File Map

### Create

- `frontend/src/shared/styles/_tokens.scss`: visual theme values shared by Bootstrap and application styles.
- `frontend/src/shared/styles/_bootstrap-theme.scss`: Bootstrap variable overrides and Bootstrap Sass import.
- `frontend/src/shared/styles/_global.scss`: reset, global shell, header/footer, and existing application-specific rules migrated from `styles.css`.
- `frontend/src/shared/styles/index.scss`: the single global style entry point.
- `frontend/src/shared/components/AsyncStatus.tsx`: shared accessible loading and API-error presentation.
- `frontend/src/shared/components/AsyncStatus.test.tsx`: focused behavior tests for loading and error variants.
- `frontend/src/features/accommodation/GuesthouseGallery.test.tsx`: modal, navigation, and keyboard behavior tests.

### Modify

- `frontend/package.json`: add Bootstrap, React-Bootstrap, and Sass with exact versions.
- `frontend/package-lock.json`: lock the new dependency graph.
- `frontend/src/main.tsx`: replace the old CSS import with the Sass entry point.
- `frontend/src/app/LanguageLayout.tsx`: use responsive React-Bootstrap navigation and layout.
- `frontend/src/app/router.test.tsx`: verify mobile navigation and the migrated status semantics while preserving existing routes.
- `frontend/src/i18n/resources.ts`: add localized mobile-navigation labels.
- `frontend/src/features/accommodation/GuesthouseListPage.tsx`: use Bootstrap container/grid and shared status feedback.
- `frontend/src/features/accommodation/GuesthouseDetailPage.tsx`: use Bootstrap layout and shared status feedback.
- `frontend/src/features/accommodation/GuesthouseGallery.tsx`: replace the custom lightbox shell with React-Bootstrap Modal and buttons.
- `docs/architecture/frontend-architecture.md`: record React-Bootstrap, Bootstrap Sass, and the styling boundary.

### Delete

- `frontend/src/styles.css`: replaced by the Sass style entry point.

---

### Task 1: Install the stable styling dependencies and establish the theme

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/package-lock.json`
- Modify: `frontend/src/main.tsx`
- Create: `frontend/src/shared/styles/_tokens.scss`
- Create: `frontend/src/shared/styles/_bootstrap-theme.scss`
- Create: `frontend/src/shared/styles/_global.scss`
- Create: `frontend/src/shared/styles/index.scss`
- Delete: `frontend/src/styles.css`

**Interfaces:**
- Consumes: the existing `frontend/src/styles.css` rules and existing `main.tsx` global import.
- Produces: a single `frontend/src/shared/styles/index.scss` global style entry point and Bootstrap utility/component classes available throughout the app.

- [ ] **Step 1: Capture the passing frontend baseline**

Run from `frontend/` with Node 24:

```bash
npm run format:check
npm run lint
npm run test
npm run build
```

Expected: format, lint, all five existing tests, and production build pass.

- [ ] **Step 2: Install exact stable dependencies**

Run:

```bash
npm install --save-exact bootstrap@5.3.8 react-bootstrap@2.10.10
npm install --save-dev --save-exact sass@1.102.0
```

Expected: `package.json` and `package-lock.json` contain the exact versions; no prerelease React-Bootstrap version appears.

- [ ] **Step 3: Create the theme tokens**

Create `frontend/src/shared/styles/_tokens.scss`:

```scss
$paper: #f4efe4;
$paper-light: #fbf8f1;
$ink: #27241f;
$muted: #6e665b;
$forest: #29493a;
$forest-dark: #193027;
$brick: #a84930;
$gold: #c8984c;
$line: rgba(39, 36, 31, 0.18);
$serif: 'Iowan Old Style', Baskerville, 'Palatino Linotype', Palatino, serif;
$sans-serif: 'Avenir Next', Avenir, 'Segoe UI', sans-serif;
$shadow: 0 24px 60px rgba(47, 38, 27, 0.16);
```

- [ ] **Step 4: Bind the tokens to Bootstrap**

Create `frontend/src/shared/styles/_bootstrap-theme.scss`:

```scss
@use 'tokens' as *;

$primary: $forest;
$secondary: $gold;
$danger: $brick;
$body-bg: $paper;
$body-color: $ink;
$font-family-sans-serif: $sans-serif;
$border-color: $line;
$border-radius: 0;
$border-radius-sm: 0;
$border-radius-lg: 0;
$enable-shadows: false;

@import 'bootstrap/scss/bootstrap';
```

- [ ] **Step 5: Migrate the existing CSS and remove the striped background**

Move all application-specific rules from `frontend/src/styles.css` into `frontend/src/shared/styles/_global.scss`. Replace the existing `:root` values with interpolated token values, and change the body background from:

```css
background:
  radial-gradient(circle at 10% 5%, rgba(200, 152, 76, 0.12), transparent 24rem),
  linear-gradient(90deg, rgba(41, 73, 58, 0.025) 1px, transparent 1px), var(--paper);
background-size:
  auto,
  48px 48px,
  auto;
```

to:

```scss
background:
  radial-gradient(circle at 10% 5%, rgba($gold, 0.12), transparent 24rem),
  $paper;
```

Keep the subtle `body::before` grain because it is non-striped. Remove `frontend/src/styles.css` after all rules have moved.

- [ ] **Step 6: Create the single Sass entry point**

Create `frontend/src/shared/styles/index.scss`:

```scss
@use 'bootstrap-theme';
@use 'global';
```

Change `frontend/src/main.tsx` from:

```ts
import './styles.css'
```

to:

```ts
import './shared/styles/index.scss'
```

- [ ] **Step 7: Verify the theme compiles before component migration**

Run:

```bash
npm run format:check
npm run lint
npm run test
npm run build
```

Expected: all checks pass; the build includes Bootstrap styling; `rg "linear-gradient\(90deg" src` returns no result.

- [ ] **Step 8: Check dependency security state**

Run:

```bash
npm audit
```

Expected: no more than the baseline three high-severity findings, and no finding introduced by Bootstrap, React-Bootstrap, or Sass.

- [ ] **Step 9: Commit the theme foundation**

```bash
git add frontend/package.json frontend/package-lock.json frontend/src/main.tsx frontend/src/shared/styles frontend/src/styles.css
git commit -m "feat(NFR-TECH-001): add React-Bootstrap theme foundation"
```

---

### Task 2: Add a mobile-first React-Bootstrap navigation shell

**Files:**
- Modify: `frontend/src/app/LanguageLayout.tsx`
- Modify: `frontend/src/app/router.test.tsx`
- Modify: `frontend/src/i18n/resources.ts`
- Modify: `frontend/src/shared/styles/_global.scss`

**Interfaces:**
- Consumes: `Language`, `SUPPORTED_LANGUAGES`, `languagePath()`, and the Bootstrap theme from Task 1.
- Produces: a controlled `Navbar`/`Offcanvas` navigation that closes after selecting a language and retains `preferredLanguage` routing behavior.

- [ ] **Step 1: Write the failing navigation interaction test**

Add `userEvent` to `frontend/src/app/router.test.tsx` and add:

```tsx
it('opens and closes the mobile language navigation', async () => {
  const user = userEvent.setup()
  renderRoute('/hu')

  const toggle = await screen.findByRole('button', { name: 'Menü megnyitása' })
  await user.click(toggle)

  expect(screen.getByRole('dialog', { name: 'Nyelvválasztás' })).toBeVisible()
  await user.click(screen.getByRole('button', { name: 'Menü bezárása' }))
  await waitFor(() =>
    expect(screen.queryByRole('dialog', { name: 'Nyelvválasztás' })).not.toBeInTheDocument(),
  )
})
```

Install `@testing-library/user-event` as an exact dev dependency only if it is not already present transitively:

```bash
npm install --save-dev --save-exact @testing-library/user-event
```

- [ ] **Step 2: Run the new test and verify it fails**

Run:

```bash
npm run test -- src/app/router.test.tsx
```

Expected: FAIL because the menu buttons and dialog do not exist.

- [ ] **Step 3: Add localized navigation labels**

Add the following keys under `app.navigation` in every language:

```ts
// hu
menu: 'Menü megnyitása',
closeMenu: 'Menü bezárása',

// ro
menu: 'Deschide meniul',
closeMenu: 'Închide meniul',

// en
menu: 'Open menu',
closeMenu: 'Close menu',
```

- [ ] **Step 4: Implement the controlled React-Bootstrap shell**

In `LanguageLayout.tsx`, import `useState` and:

```tsx
import { Container, Nav, Navbar, Offcanvas } from 'react-bootstrap'
```

Add:

```tsx
const [navigationOpen, setNavigationOpen] = useState(false)
```

Use a `Navbar` with `expand="md"`, a `Navbar.Toggle`, and `Navbar.Offcanvas`. Render the brand as `Navbar.Brand as={Link}`, language links as `Nav.Link as={Link}`, and close the controlled offcanvas in every language-link `onClick`. Preserve the existing skip link, `aria-current`, route replacement, `document.documentElement.lang`, and footer content.

- [ ] **Step 5: Adapt shell styling without changing its identity**

Update `_global.scss` so `.site-header` styles the Bootstrap navbar rather than a plain header. Ensure:

```scss
.navbar-toggler {
  min-width: 44px;
  min-height: 44px;
  border-color: var(--line);
}

.offcanvas {
  background: var(--paper-light);
}
```

Keep the existing brand mark, typography, border line, and footer treatment.

- [ ] **Step 6: Run the focused and complete checks**

Run:

```bash
npm run test -- src/app/router.test.tsx
npm run format:check
npm run lint
npm run build
```

Expected: navigation test and the five existing route behaviors pass; build succeeds.

- [ ] **Step 7: Commit the responsive shell**

```bash
git add frontend/src/app/LanguageLayout.tsx frontend/src/app/router.test.tsx frontend/src/i18n/resources.ts frontend/src/shared/styles/_global.scss frontend/package.json frontend/package-lock.json
git commit -m "feat(NFR-TECH-001): add mobile Bootstrap navigation"
```

---

### Task 3: Standardize loading and error feedback

**Files:**
- Create: `frontend/src/shared/components/AsyncStatus.tsx`
- Create: `frontend/src/shared/components/AsyncStatus.test.tsx`
- Modify: `frontend/src/features/accommodation/GuesthouseListPage.tsx`
- Modify: `frontend/src/features/accommodation/GuesthouseDetailPage.tsx`
- Modify: `frontend/src/shared/styles/_global.scss`

**Interfaces:**
- Consumes: localized message strings passed by feature pages.
- Produces: `AsyncStatus({ variant, message }: { variant: 'loading' | 'error'; message: string })`.

- [ ] **Step 1: Write failing shared-status tests**

Create `AsyncStatus.test.tsx`:

```tsx
import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import AsyncStatus from './AsyncStatus'

describe('AsyncStatus', () => {
  it('announces loading without presenting an error', () => {
    render(<AsyncStatus variant="loading" message="Betöltés" />)
    expect(screen.getByRole('status')).toHaveTextContent('Betöltés')
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('presents failures as an alert', () => {
    render(<AsyncStatus variant="error" message="Hiba" />)
    expect(screen.getByRole('alert')).toHaveTextContent('Hiba')
  })
})
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run:

```bash
npm run test -- src/shared/components/AsyncStatus.test.tsx
```

Expected: FAIL because `AsyncStatus.tsx` does not exist.

- [ ] **Step 3: Implement the minimal accessible component**

Create `AsyncStatus.tsx` using React-Bootstrap `Alert` and `Spinner`:

```tsx
import Alert from 'react-bootstrap/Alert'
import Spinner from 'react-bootstrap/Spinner'

interface AsyncStatusProps {
  variant: 'loading' | 'error'
  message: string
}

export default function AsyncStatus({ variant, message }: AsyncStatusProps) {
  if (variant === 'error') {
    return <Alert variant="danger">{message}</Alert>
  }

  return (
    <div className="async-status" role="status">
      <Spinner animation="border" size="sm" aria-hidden="true" />
      <span>{message}</span>
    </div>
  )
}
```

- [ ] **Step 4: Run the focused test and verify it passes**

Run:

```bash
npm run test -- src/shared/components/AsyncStatus.test.tsx
```

Expected: two tests pass.

- [ ] **Step 5: Replace duplicated page status markup**

Use `AsyncStatus` in `GuesthouseListPage.tsx` and `GuesthouseDetailPage.tsx`. Keep the detail-page back link after the error alert. Add only the minimal `.async-status` spacing/alignment rules to `_global.scss`.

- [ ] **Step 6: Run all frontend tests**

Run:

```bash
npm run test
npm run lint
```

Expected: existing route tests and the two new status tests pass.

- [ ] **Step 7: Commit status feedback**

```bash
git add frontend/src/shared/components frontend/src/features/accommodation/GuesthouseListPage.tsx frontend/src/features/accommodation/GuesthouseDetailPage.tsx frontend/src/shared/styles/_global.scss
git commit -m "feat(NFR-TECH-001): add accessible Bootstrap status feedback"
```

---

### Task 4: Migrate public page layout to the Bootstrap grid

**Files:**
- Modify: `frontend/src/features/accommodation/GuesthouseListPage.tsx`
- Modify: `frontend/src/features/accommodation/GuesthouseDetailPage.tsx`
- Modify: `frontend/src/shared/styles/_global.scss`
- Test: `frontend/src/app/router.test.tsx`

**Interfaces:**
- Consumes: existing guesthouse DTOs, links, translations, and Bootstrap theme.
- Produces: the same route content arranged with `Container`, `Row`, and `Col` at Bootstrap breakpoints.

- [ ] **Step 1: Strengthen the regression assertions before layout changes**

In `router.test.tsx`, assert that both list-card links still point to their language-prefixed detail URLs and that the detail back link points to `/hu`:

```tsx
expect(screen.getByRole('link', { name: /Nisztor Panzió/ })).toHaveAttribute(
  'href',
  '/hu/guesthouses/nisztor-panzio',
)
expect(screen.getByRole('link', { name: '← Vissza a panziókhoz' })).toHaveAttribute('href', '/hu')
```

- [ ] **Step 2: Run the strengthened test**

Run:

```bash
npm run test -- src/app/router.test.tsx
```

Expected: PASS before migration, proving the assertions describe existing behavior.

- [ ] **Step 3: Replace only structural layout with Bootstrap primitives**

In the list page:

- wrap hero and list content in appropriately sized `Container` elements;
- replace `.guesthouse-grid` display-grid responsibility with `Row`;
- wrap each custom `article.guesthouse-card` in `Col xs={12} lg={6}`;
- keep the article, image, card number, typography, and links unchanged.

In the detail page:

- use `Container` for the back row, hero, story, and gallery boundaries;
- use `Row`/`Col` only where it simplifies the existing desktop-to-mobile transition;
- keep the custom detail hero and gallery grid.

- [ ] **Step 4: Remove superseded custom grid declarations**

Delete only CSS declarations now supplied by Bootstrap, such as the old `display: grid` and two-column definitions on `.guesthouse-grid`. Retain spacing, card animation, image ratios, typography, and the mobile gallery rules.

- [ ] **Step 5: Run regression tests and build**

Run:

```bash
npm run test
npm run lint
npm run build
```

Expected: all tests pass and the production build succeeds.

- [ ] **Step 6: Commit the grid migration**

```bash
git add frontend/src/features/accommodation/GuesthouseListPage.tsx frontend/src/features/accommodation/GuesthouseDetailPage.tsx frontend/src/shared/styles/_global.scss frontend/src/app/router.test.tsx
git commit -m "refactor(NFR-TECH-001): use Bootstrap public page layout"
```

---

### Task 5: Replace the custom lightbox shell with React-Bootstrap Modal

**Files:**
- Create: `frontend/src/features/accommodation/GuesthouseGallery.test.tsx`
- Modify: `frontend/src/features/accommodation/GuesthouseGallery.tsx`
- Modify: `frontend/src/shared/styles/_global.scss`

**Interfaces:**
- Consumes: `GuesthouseImage[]` and existing `guesthouses.*` translations.
- Produces: a Bootstrap `Modal` gallery with the same image selection, cyclic previous/next navigation, image counter, and Escape close behavior.

- [ ] **Step 1: Write failing gallery interaction tests**

Create a two-image fixture and tests using `userEvent`:

```tsx
it('opens the selected image in a modal and navigates cyclically', async () => {
  const user = userEvent.setup()
  renderWithI18n(<GuesthouseGallery images={images} />)

  await user.click(screen.getByRole('button', { name: 'Első kép' }))
  expect(screen.getByRole('dialog', { name: 'Első kép' })).toBeVisible()

  await user.click(screen.getByRole('button', { name: 'Következő kép' }))
  expect(screen.getByText('2 / 2')).toBeVisible()

  await user.click(screen.getByRole('button', { name: 'Következő kép' }))
  expect(screen.getByText('1 / 2')).toBeVisible()
})

it('closes the modal with Escape', async () => {
  const user = userEvent.setup()
  renderWithI18n(<GuesthouseGallery images={images} />)
  await user.click(screen.getByRole('button', { name: 'Első kép' }))
  await user.keyboard('{Escape}')
  await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
})
```

The local `renderWithI18n` helper wraps the gallery in the existing `AppProviders`.

- [ ] **Step 2: Run the tests and verify the Modal expectation fails**

Run:

```bash
npm run test -- src/features/accommodation/GuesthouseGallery.test.tsx
```

Expected: FAIL because the current lightbox is not a React-Bootstrap Modal and does not provide the planned close behavior.

- [ ] **Step 3: Implement the React-Bootstrap Modal**

Import `Button` and `Modal`. Keep `selectedIndex` as the single state value. Use:

```tsx
<Modal
  show={selectedIndex !== null}
  onHide={() => setSelectedIndex(null)}
  centered
  size="xl"
  fullscreen="sm-down"
  aria-label={selectedImage?.altText}
>
```

Render a `Modal.Header closeButton closeLabel={t('guesthouses.closeGallery')}>`, `Modal.Body`, the existing figure/counter, and previous/next `Button` controls. Keep the document key handler only for left/right arrows; allow Modal to own Escape and focus restoration.

- [ ] **Step 4: Remove obsolete custom dialog-shell CSS**

Delete the fixed `.lightbox` overlay, manual close positioning, and focus rules supplied by Modal. Keep image sizing, figure caption, and branded previous/next button overrides scoped to the gallery modal.

- [ ] **Step 5: Run focused and complete checks**

Run:

```bash
npm run test -- src/features/accommodation/GuesthouseGallery.test.tsx
npm run test
npm run format:check
npm run lint
npm run build
```

Expected: gallery tests, route tests, formatting, lint, and build all pass.

- [ ] **Step 6: Commit the modal migration**

```bash
git add frontend/src/features/accommodation/GuesthouseGallery.tsx frontend/src/features/accommodation/GuesthouseGallery.test.tsx frontend/src/shared/styles/_global.scss
git commit -m "refactor(NFR-TECH-001): use Bootstrap gallery modal"
```

---

### Task 6: Document the frontend styling boundary and perform final QA

**Files:**
- Modify: `docs/architecture/frontend-architecture.md`
- Verify: all frontend files changed by Tasks 1-5

**Interfaces:**
- Consumes: the completed React-Bootstrap migration.
- Produces: documented rules for future public, booking, and administration features.

- [ ] **Step 1: Update frontend architecture documentation**

Add a styling section that records:

- React-Bootstrap is the React component and responsive layout foundation;
- Bootstrap is themed through Sass using project tokens;
- Bootstrap JavaScript and CDN assets are forbidden;
- feature-specific visual identity remains in scoped application styles;
- new forms, navigation, modal, alert, and responsive grid behavior should prefer React-Bootstrap;
- wrappers are introduced only for repeated application-level behavior or visual policy.

- [ ] **Step 2: Run the complete frontend quality gate without shortcuts**

Run:

```bash
npm run format:check
npm run lint
npm run test
npm run build
npm audit
```

Expected: format, lint, every test, and build pass; audit is not worse than the three-high baseline and introduces no Bootstrap-related finding.

- [ ] **Step 3: Start the application for visual inspection**

With the backend and PostgreSQL available, run:

```bash
npm run dev
```

Inspect `/hu`, `/hu/guesthouses/nisztor-panzio`, and `/hu/guesthouses/bukovina-panzio` at 320, 768, and 1280 pixel viewport widths.

Expected:

- no striped/grid background;
- no horizontal page scroll;
- mobile menu opens, closes, and changes language;
- both guesthouse cards remain visually distinct;
- detail content and all gallery controls remain usable;
- modal fits the mobile viewport;
- keyboard Tab, Escape, Left, and Right behavior works;
- the current paper/forest/gold/brick identity remains recognizable.

- [ ] **Step 4: Review the final diff for unintended migration scope**

Run:

```bash
git diff --check
git status --short
git diff --stat
```

Expected: only the planned frontend infrastructure, frontend component, test, and architecture documentation files changed.

- [ ] **Step 5: Commit documentation and final cleanup**

```bash
git add docs/architecture/frontend-architecture.md frontend
git commit -m "docs(NFR-TECH-001): document React-Bootstrap frontend styling"
```

## Plan Self-Review Result

- Spec coverage: dependency choice, stable-version rule, Sass theme, preserved visual identity, striped-background removal, mobile navigation, status handling, Bootstrap grid, modal accessibility, tests, documentation, and visual QA are mapped to Tasks 1-6.
- Placeholder scan: no `TBD`, `TODO`, deferred implementation instruction, or undefined helper remains.
- Type consistency: `AsyncStatus` props, `navigationOpen` state, language label keys, and gallery `selectedIndex` behavior are consistent across tasks.
- Scope: the plan changes frontend infrastructure and existing screens only; it does not implement admin or new business functionality.
