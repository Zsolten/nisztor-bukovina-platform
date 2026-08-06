# Fullscreen Homepage Hero Design

## Goal

Make the existing multilingual homepage hero fill the first viewport while preserving its four-image, 24-second slideshow. The navigation overlays the hero with a transparent blurred treatment at the top of the page and changes to the existing light treatment after scrolling.

## Scope

- Keep the current four hero images and six-second interval per image.
- Keep the existing hero title and two guesthouse logos.
- Make the hero at least one small viewport height (`100svh`) on desktop and mobile.
- Place the fixed header over the hero instead of reserving space above it.
- Use a transparent blurred header with light navigation content at the top of the page.
- Switch the header to an opaque light background with dark content after the user scrolls beyond a small threshold.
- Remove the artificial light rectangles and padding from the transparent logo images.
- Increase the hero logo and title scale without allowing text or controls to overflow.

## Component Design

`LanguageLayout` owns a boolean scrolled state because the header is shared across all localized routes. A passive window scroll listener updates that state and adds a `site-header-scrolled` modifier class. The state is initialized from the current scroll position and the listener is removed on unmount.

`HomepageHero` keeps its current markup and slideshow data. No carousel dependency, timer state or additional API call is introduced.

SCSS owns the visual states:

- `.site-header` is fixed, transparent and blurred by default.
- `.site-header-scrolled` restores the light background, dark text, border and subtle shadow.
- `.guesthouse-hero` uses `min-height: 100svh`.
- hero content includes safe top padding for the overlaid header.
- transparent logo PNGs use no background panel and receive only a restrained drop shadow.

Non-home routes also start with the transparent header only while at scroll position zero. Their existing hero media provides the backdrop; scrolling produces the light header consistently.

## Accessibility And Motion

- Navigation controls retain their 44-pixel minimum target size.
- Light and dark header states maintain readable contrast.
- Existing reduced-motion behavior continues to freeze the slideshow on its first image.
- The header state change does not trap focus or alter navigation semantics.

## Verification

- Add a layout test that dispatches a scroll event and verifies the header modifier class.
- Keep the existing assertion that four hero slides are rendered.
- Run frontend lint, all Vitest tests and the production build.
- Inspect the homepage at desktop and mobile widths, at the top and after scrolling.
- Confirm the header changes state, the hero fills the viewport, logos have no rectangular background and no horizontal overflow appears.
