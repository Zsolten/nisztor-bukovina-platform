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

---

# Tourism mobile carousel follow-up QA

## Evidence

- Source visual truth:
  - `C:\Users\nisto\Downloads\csillagtura_phone_mockup_2.png` (top section, 852 x 506 pixels).
  - `C:\Users\nisto\Downloads\csillagtura_phone_mockup.png` (map and carousel, 852 x 1536 pixels).
- Browser-rendered implementation:
  - `frontend/.design-qa/tourism-mobile-top.png`.
  - `frontend/.design-qa/tourism-mobile-map-carousel.png`.
- Route: `http://localhost:5173/hu/star-tours`.
- Viewport: 426 x 768 CSS pixels, device scale factor 1.
- Density normalization: the 852-pixel-wide source mockups were evaluated as 2x captures of a 426 CSS-pixel mobile layout. The implementation captures are 426 pixels wide at 1x.
- State: Hungarian tour view with two published tours, both cached routes, all attraction pins, the guesthouse pin, and the second tour selected.
- Full-view comparison: each source image and its corresponding implementation capture were emitted together in the same browser comparison result.
- Focused interaction checks: pagination selected and centered the second card; a real upward drag on the selected card opened the full-screen details dialog; the dialog measured 426 x 768 CSS pixels.

## Findings

- No actionable P0/P1/P2 difference remains in the requested mobile carousel behavior.
- [P3] The Google basemap palette and label density differ from the illustrated source map.
  - Evidence: the implementation uses the configured Dynamic Maps basemap while the mockup uses a muted editorial map treatment.
  - Impact: route and pin contrast remain clear, but the overall map tone is less parchment-like.
  - Follow-up: apply a muted cloud map style to the configured Map ID when the visual styling ticket is opened.

## Required fidelity surfaces

- Fonts and typography: the existing serif display and compact sans-serif UI hierarchy are preserved; mobile title wrapping is intentional at real phone width.
- Spacing and layout rhythm: the top section follows the mockup order; the map occupies a fixed mobile region; cards are equal-height, snap horizontally, and expose the next card edge.
- Colors and visual tokens: forest, paper, brick, route colors, selected border, and route glow remain consistent with the product tokens.
- Image quality and asset fidelity: tour images come from the existing repository/API content and use a stable cropped card slot; no placeholder drawing was introduced.
- Copy and content: Hungarian API content is shown without fallback translation; intermediate stops use the compact omitted-middle presentation.

## Comparison history

1. Initial implementation stacked mobile cards vertically and hid the tourism header.
2. Added a horizontal snap carousel, fixed card height, compact mobile header, pagination dots, and upward-swipe details.
3. First visual comparison found an oversized card and overlapping stop text because the absolutely positioned list was anchored to the copy column.
4. Reduced the card width to reveal the neighboring card, made the header scroll away, anchored stops to the card, and rebalanced the card to a fixed 280-pixel height.
5. Post-fix evidence measured two 358 x 280 cards, carousel `scrollWidth` 770 over `clientWidth` 411, no document-level horizontal overflow, and a full-screen 426 x 768 details dialog.

## Implementation checklist

- Horizontal rightward card scrolling: passed.
- Fixed and equal mobile card heights: passed.
- Minimal header that leaves the viewport while scrolling: passed.
- Pagination selection and card centering: passed.
- Upward drag opens full-screen tour details: passed.
- Mobile page horizontal overflow: passed.

final result: passed

---

# Tourism map follow-up QA

## Evidence

- Source visual truth: `C:\Users\nisto\Downloads\csillagtura_desktop_mockup.png`.
- Browser-rendered implementation: `frontend/.design-qa/tourism-detail-latest.jpg`, captured at `http://localhost:5173/hu/star-tours/maros-mente-es-gyulafehervar`.
- Full-view comparison: source and implementation were emitted together for comparison in the in-app browser at desktop density.
- Checked interaction: a public tour detail loads its title, long description, total distance and time, every stop, and each stop's external map link. Browser console: no warnings or errors.

## Findings

- [P1] The new all-routes map state cannot be rendered against the source mockup in the current local runtime.
  - Evidence: the running backend returns `404` for `/api/tourism/star-tours/routes`; it has not been restarted since the new endpoint was added. The current frontend consequently shows its request-error state.
  - Impact: the initial all-tour polylines, marker fit, stacked stop previews, and list-to-detail transition cannot receive an end-to-end visual pass yet.
  - Fix: restart the Spring Boot backend, then capture the desktop and mobile tour views with the two active Hungarian tours.
- [P2] The current public tour records have no gallery image data.
  - Evidence: both returned public tours have `images: []`; the implementation only renders the gallery from API images, as intended.
  - Impact: the responsive gallery behaviour has unit coverage but cannot be checked using live content.
  - Fix: add one or more Hungarian-alt-text images in the tourism admin for each tour, then repeat the detail capture.

## Required fidelity surfaces

- Fonts and typography: the detail view reuses the mockup's serif display hierarchy and the existing compact navigation styling.
- Spacing and layout rhythm: the detail page keeps the source's paper/forest editorial rhythm and presents stops in a separate, scannable numbered panel.
- Colors and visual tokens: route-stop markers inherit each tour's configured `mapColor`; paper, forest, and line tokens remain consistent with the map screen.
- Image quality and asset fidelity: gallery uses only backend-provided images and translated alt text; no placeholder image is shown for missing content.
- Copy and content: long description and all stop names come from the requested-language public API response.

## Implementation checklist

1. Restart the local Spring Boot backend.
2. Open `/hu/star-tours` and verify both colored cached routes and all attraction markers on first load.
3. Add tour gallery images in admin, then recheck desktop/mobile details.

final result: blocked

---

# Latest tourism mobile QA status

The earlier runtime blocker is resolved: the local backend now serves the cached route endpoint, both Hungarian tours render, the Dynamic Map loads, and the mobile carousel follow-up above completed successfully.

final result: passed
