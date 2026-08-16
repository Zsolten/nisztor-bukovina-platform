# Design QA - Guesthouse detail layout

## Comparison target

- Source visual truth: https://www.palkonyha.hu/szallas/
- Implementation: http://localhost:5173/hu/guesthouses/nisztor-panzio
- Source screenshot path: in-app browser capture emitted from the source URL in this task.
- Implementation screenshot path: in-app browser capture emitted from the local URL in this task.
- State: Hungarian Nisztor guesthouse detail page, editorial accommodation section.
- Viewport: source and implementation at 1280 x 720 CSS pixels, DPR 1.
- Pixel dimensions: both captures 1280 x 720; no density normalization required.

## Full-view comparison evidence

- The reference and implementation were emitted together in one comparison result at the same viewport and equivalent content position.
- Both use two tall images beside a restrained text column with generous whitespace and a visible booking CTA.
- The implementation intentionally retains the project's forest, paper, serif typography, header, and backend-provided content.
- No horizontal overflow was measured at the checked viewport.

## Focused region evidence

- Dining: both supplied food images loaded at their natural resolution and use the same two-image editorial rhythm.
- Services: four categories are visually separated by column and row dividers; individual amenities no longer merge into pill tags.
- Rooms and pricing: headings, descriptions, room cards, price rows, and the second booking CTA are visibly separated and aligned.
- Gallery: ten items rendered, the modal dialog opened from a gallery item, and no browser console errors were recorded.

## Comparison history

- P2: the pricing booking CTA was initially placed after the long price list and was not visible with the section heading.
- Fix: moved the CTA into the pricing header and added responsive grid placement.
- Post-fix evidence: one booking CTA is visible when the pricing section is aligned to the viewport; horizontal overflow remains zero.

## Intentional differences

- The source gallery appears prominently near the start. The implementation keeps the gallery at the bottom at the product owner's request.
- Historical copy is omitted. The page uses the current guesthouse description and a dedicated homemade-food section.
- Booking buttons are deliberately marked `aria-disabled` and do not trigger navigation until the booking flow is implemented.

## Follow-up polish

- P3: repeat the focused visual capture at the existing 620 px breakpoint when a resizable browser viewport is available. Responsive rules, lint, tests, and the production build are currently passing.

## Final result

passed

---

# Tourism map design QA

## Evidence

- Source visual truth:
  - `C:\Users\nisto\Downloads\csillagtura_desktop_mockup.png`
  - `C:\Users\nisto\Downloads\Latnivalok_phone_mockup.png`
- Browser-rendered implementation:
  - `frontend/.design-qa/tourism-desktop.png`
  - `frontend/.design-qa/tourism-mobile.png`
- Combined comparison evidence:
  - `frontend/.design-qa/comparison-desktop.png`
  - `frontend/.design-qa/comparison-mobile.png`
- Route: `http://localhost:5173/hu/star-tours`
- Desktop viewport: 1512 x 982 CSS pixels, device scale factor 1.
- Desktop source: 1536 x 1067 pixels. The implementation was normalized to 1536 x 998 for the side-by-side comparison.
- Mobile viewport: 393 x 852 CSS pixels, device scale factor 1.
- Mobile source: 852 x 1852 pixels. The implementation was normalized to 852 x 1847 for the side-by-side comparison.
- States checked: desktop tour view without map configuration, mobile attraction list, attraction search, category filtering, expandable details, and responsive navigation.
- Browser console: no errors or warnings in the checked states.

## Findings

- [P1] The live Dynamic Map cannot be visually compared yet.
  - Evidence: `VITE_GOOGLE_MAPS_API_KEY` is not configured, so the intentional setup state is rendered instead of Google Maps.
  - Impact: map styling, markers, cached route polylines, viewport fitting, and map controls cannot receive a visual pass.
  - Fix: add a browser-restricted Maps JavaScript API key to `frontend/.env`, restart Vite, then repeat desktop and mobile map capture.
- [P1] The live tour-card state cannot be compared to the desktop and mobile tour mockups.
  - Evidence: `/api/tourism/star-tours?lang=hu` currently returns an empty array, while the attraction endpoint returns ten Hungarian entries.
  - Impact: selected tour cards, route colors, route legend, favorite states, and the mobile tour carousel cannot receive a live visual pass.
  - Fix: publish at least one active Hungarian star tour in the admin UI, then repeat the tour-view capture.
- [P3] The mobile introduction wraps onto more lines than the wide source mockup.
  - Evidence: the implementation uses the longer approved introductory copy at a 393 px viewport.
  - Impact: content below the tabs starts slightly lower; the hierarchy remains intact.
  - Follow-up: shorten the mobile-only introduction if a tighter first viewport is preferred.

## Required fidelity surfaces

- Fonts and typography: serif display and compact sans-serif UI hierarchy match the existing product tokens and source direction; live map labels remain unchecked.
- Spacing and layout rhythm: desktop split view and mobile stacked list match the source structure; map/card overlay remains unchecked without published tours.
- Colors and visual tokens: forest, paper, brick, gold, borders, and focus rings align with the source and existing design system.
- Image quality and asset fidelity: existing destination photography is used directly; no placeholder or generated raster asset was introduced.
- Copy and content: Hungarian UI copy is complete; tourism entity content is rendered only when the requested backend translation exists.

## Comparison history

1. Initial mobile pass showed the language indicator missing and all attraction cards without image support.
2. Added the mobile language indicator and image-aware attraction cards using existing repository assets.
3. The revised mobile comparison has no remaining P0/P1/P2 layout finding. Full map and tour-card fidelity are still blocked by configuration and live data state.

## Implementation checklist

- Configure the separate frontend Maps JavaScript API key and optional JavaScript Map ID.
- Publish one active Hungarian star tour.
- Repeat map, marker, route, selected-tour, and mobile carousel captures.

final result: blocked
