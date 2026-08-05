# Homepage content expansion design

## Goal

Enrich the public homepage without introducing a new framework or backend feature. The existing multilingual React, React-Bootstrap, routing, and visual system remain in place.

## Scope

- Keep the current welcome hero copy and reduce oversized display text slightly.
- Make the existing navigation sticky while preserving mobile offcanvas behavior.
- Keep the guesthouse selection as the primary homepage action.
- Add a family legacy section using the supplied `about_us` photograph and the approved near-30-year hospitality text.
- Add four concise excursion-direction items using the supplied Deva, Sibiu, Turda and Retezat imagery.
- Add one clearly presented temporary guest-review mock.
- Add a map section for Cristur at the bottom of the homepage.
- Normalize newly supplied image filenames to lowercase descriptive kebab-case names.

## Page order

1. Sticky navigation
2. Existing welcome hero
3. Guesthouse selection
4. Family legacy
5. Suggested excursion directions
6. Temporary guest review
7. Location map
8. Existing footer

This order follows the content progression of the Transylvanian Inn reference while retaining the restrained hover and reveal interactions already present in the project and visible on the Palkonyha reference.

## Components

The homepage remains owned by the accommodation feature. The new static editorial sections are split into focused components:

- `HomepageLegacy`: family image and multilingual legacy copy.
- `HomepageSurroundings`: four responsive destination items with images.
- `HomepageReview`: one temporary testimonial clearly isolated for later replacement.
- `HomepageMap`: responsive embedded map and external map link.

No new API or persistence model is introduced. Guesthouse cards continue to use the current backend response.

## Images

New files are renamed without altering their visual content. Guesthouse photographs use a guesthouse-specific prefix and sequence; destination photographs use recognizable location names. Existing user deletions remain untouched. All displayed images receive translated alternative text.

## Responsive behavior

- Desktop uses alternating editorial image/text layouts and a four-item surroundings grid.
- Tablet uses two columns where space allows.
- Mobile uses a single reading column, preserves stable image aspect ratios, and keeps all interactive targets at least 44 px high.
- Motion is limited to existing-style image scaling and subtle reveal transitions, with reduced-motion support preserved.

## Testing

- Extend the homepage component/router test to verify the legacy, surroundings, mock review and map sections.
- Run Prettier, ESLint, Vitest and the production build.
- Verify the rendered homepage at desktop and mobile widths after starting the Vite development server.

## Out of scope

- Persisted or admin-managed reviews.
- Tourism detail pages and itinerary generation.
- A dedicated map provider integration or API key.
- Booking actions.
