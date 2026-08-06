# Fullscreen Homepage Hero Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the existing timed homepage slideshow into a full-viewport opening scene with a transparent blurred header that becomes light after scrolling.

**Architecture:** `LanguageLayout` derives one boolean from `window.scrollY` and exposes it as a header modifier class. The slideshow remains a CSS animation owned by `HomepageHero`; SCSS controls viewport height, header states, logo transparency and responsive sizing.

**Tech Stack:** React 19, TypeScript 6, React-Bootstrap 2, SCSS, Vitest and Testing Library.

## Global Constraints

- Keep the current four hero images and 24-second slideshow cycle.
- Do not add dependencies or JavaScript carousel state.
- Keep navigation controls at least 44 by 44 pixels.
- Preserve HU, RO and EN routes and the existing reduced-motion behavior.
- Keep the header readable at the top of the hero and after scrolling.

---

### Task 1: Add the scroll-aware header state

**Files:**
- Modify: `frontend/src/app/LanguageLayout.tsx`
- Test: `frontend/src/app/router.test.tsx`

**Interfaces:**
- Consumes: `window.scrollY` and the browser `scroll` event.
- Produces: the `site-header-scrolled` class on the existing `Navbar` when `window.scrollY > 24`.

- [ ] **Step 1: Write the failing header-state test**

Render `/hu`, set `window.scrollY` to `25`, dispatch a `scroll` event and assert that `.site-header` has `site-header-scrolled`. Reset `scrollY` to zero and verify the class is absent after another scroll event.

- [ ] **Step 2: Run the focused test and confirm RED**

Run `npm.cmd run test -- src/app/router.test.tsx`.

Expected: FAIL because the modifier class is never rendered.

- [ ] **Step 3: Implement the scroll listener**

Add `const [scrolled, setScrolled] = useState(() => window.scrollY > 24)` and a passive `scroll` listener inside an effect with cleanup. Compose the Navbar class as `site-header${scrolled ? ' site-header-scrolled' : ''}`.

- [ ] **Step 4: Run the focused test and confirm GREEN**

Run `npm.cmd run test -- src/app/router.test.tsx`.

Expected: all router tests pass.

### Task 2: Style the full-viewport hero and both header states

**Files:**
- Modify: `frontend/src/shared/styles/_global.scss`

**Interfaces:**
- Consumes: `.site-header-scrolled`, `.guesthouse-hero`, `.hero-content` and `.hero-logos`.
- Produces: a `100svh` hero, transparent top header, light scrolled header and transparent logo presentation.

- [ ] **Step 1: Implement the default overlay header**

Change `.site-header` to fixed positioning with a transparent blurred background, light brand/language styling and a smooth background/color transition. Add top-state styling for the mark, subtitle, language controls and mobile toggler.

- [ ] **Step 2: Implement the scrolled header modifier**

Add `.site-header-scrolled` rules that restore an opaque light background, dark brand content, forest active language state, border and subtle shadow.

- [ ] **Step 3: Expand and rebalance the hero**

Set `.guesthouse-hero` to `min-height: 100svh`, add header-aware content padding, enlarge the title and logos within responsive maximums, and remove logo padding/background rectangles while retaining a drop shadow.

- [ ] **Step 4: Preserve mobile and reduced-motion behavior**

Update existing mobile rules so the hero remains at least `100svh`, logos fit side by side, title does not overflow and the reduced-motion block still displays only the first image.

- [ ] **Step 5: Run full automated verification**

Run `npm.cmd run lint`, `npm.cmd run test` and `npm.cmd run build`.

Expected: all commands exit zero.

- [ ] **Step 6: Inspect desktop and mobile behavior**

At approximately 1440 by 900 and 390 by 844, inspect the page at scroll position zero and after scrolling. Verify the hero fills the viewport, the header changes to light, both logos have no rectangular background, content remains readable and there is no horizontal overflow.
