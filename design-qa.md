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
