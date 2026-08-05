# React-Bootstrap frontend infrastructure design

- Dátum: 2026-08-05
- Állapot: jóváhagyott
- Kapcsolódó követelmény: `NFR-TECH-001`

## Cél

A frontend a meglévő panziós arculat megtartása mellett React-Bootstrap alapú, mobil-first komponens- és layout-infrastruktúrát kap. A változtatás a jelenlegi publikus panzióoldalakat is célzottan migrálja, mert a kódbázis még elég kicsi az alacsony kockázatú átálláshoz.

## Döntés

- A React-integrációhoz a `react-bootstrap`, a stílusalaphoz a `bootstrap` csomag készül bevezetésre.
- A Bootstrap saját JavaScript bundle-je nem kerül betöltésre; az interaktív viselkedést React-Bootstrap komponensek adják.
- A Bootstrap forrás Sass változókon keresztül kapja meg a projekt meglévő szín-, tipográfiai és felületi tokenjeit.
- A fejlesztés mobil-first. A Bootstrap breakpointok, grid és reszponzív komponensek jelentik az alapot, a panzióoldal egyedi hero-, kártya- és galériamegjelenése célzott saját stílus marad.
- A migráció nem alakítja át a feature-alapú könyvtárszerkezetet, és nem hoz létre absztrakt wrappert minden Bootstrap komponens köré.

## Csomagok és verziózási elv

- Production dependency: `bootstrap`.
- Production dependency: `react-bootstrap`.
- Development dependency: `sass`.
- Productionben stabil, Bootstrap 5-kompatibilis React-Bootstrap kiadás használható. Béta kiadás csak akkor választható, ha egy szükséges React 19 kompatibilitási probléma stabil kiadással nem oldható meg, és ezt külön tesztbizonyíték indokolja.
- A csomagfrissítés után az npm security audit eredménye nem romolhat a kiinduló állapothoz képest.

## Stílusrétegek

```text
frontend/src/shared/styles/
|-- _tokens.scss
|-- _bootstrap-theme.scss
|-- _global.scss
`-- index.scss
```

- `_tokens.scss`: a projekt arculati Sass változói.
- `_bootstrap-theme.scss`: a Bootstrap változóinak felülírása, majd a Bootstrap Sass importja.
- `_global.scss`: alkalmazásszintű alapstílusok és olyan meglévő arculati elemek, amelyek nem tartoznak egyetlen feature-höz.
- `index.scss`: a stílusrétegek egyetlen belépési pontja.

A jelenlegi tokenek leképezése:

| Projekt token | Bootstrap szerep |
| --- | --- |
| `forest` | primary |
| `gold` | secondary |
| `brick` | danger |
| `paper` | body background |
| `ink` | body foreground |

A jelenlegi, függőleges csíkozást adó ismétlődő lineáris háttér teljesen megszűnik. A body megtartja a világos, meleg papírszínt és a visszafogott radiális fényhatást, de nem használ ismétlődő rácsot vagy csíkmintát.

## Komponenshasználat

A meglévő fájlokban a következő elemek migrálhatók:

- `LanguageLayout`: `Container`, valamint mobilon összecsukható `Navbar`, `Nav` és `Offcanvas`;
- lista- és részletoldal: `Container`, `Row`, `Col`, `Button`, `Spinner`, `Alert`;
- galéria: az egyedi rács megmarad, a lightbox viselkedése React-Bootstrap `Modal` alapra kerülhet;
- későbbi űrlapok: `Form`, `InputGroup`, validációs állapotok;
- későbbi adminfelület: `Table`, `Card`, `Modal`, `Accordion`, `Pagination` és `Toast`.

A panziókártyák, a hero, a képarányok, a dekoráció és az arculati tipográfia nem válik generikus Bootstrap megjelenéssé.

## Mobilhasználat

- A navigáció kis képernyőn összecsukható és érintéssel könnyen kezelhető.
- Az interaktív vezérlők célmérete legalább 44 pixel.
- A tartalom 320 pixel szélességtől vízszintes oldal-scroll nélkül használható.
- Az űrlapok mobilon egyoszloposak.
- A dialogok és galéria mobilon a rendelkezésre álló képernyőteret használják, és billentyűzettel is bezárhatók.
- A `prefers-reduced-motion` támogatás megmarad.

## Hozzáférhetőség

- A szemantikus címsorok, `main` régió, skip link és nyelvválasztó címkézése megmarad.
- React-Bootstrap komponenseknél is explicit, lokalizált accessible name szükséges, ahol a vizuális tartalom önmagában nem elég.
- A modal fókuszkezelését és bezárását automatizált teszt ellenőrzi.

## Hibakezelés

- A betöltési állapot React-Bootstrap `Spinner` és képernyőolvasó számára elérhető szöveg kombinációja.
- Az API-hiba `Alert` komponensben jelenik meg, a meglévő lokalizált szövegek használatával.
- Az üres adatállapot nem minősül API-hibának, és külön megjelenítést kap, amikor ilyen üzleti eset megjelenik.

## Tesztelés és ellenőrzés

- A meglévő route- és tartalmi tesztek változatlan üzleti viselkedést bizonyítanak.
- Új teszt ellenőrzi a mobil navigáció megnyitását és bezárását.
- A galéria tesztje ellenőrzi a modal megnyitását, a következő/előző képet és az Escape bezárást.
- A frontend teljes minőségi kapuja: format, lint, Vitest és production build.
- Kézi vizuális ellenőrzés szükséges legalább 320, 768 és 1280 pixel szélességen.

## Migrációs stratégia

1. A dependencyk és a Sass belépési pont bevezetése.
2. A meglévő tokenek Bootstrap témába kötése.
3. A csíkos body-háttér eltávolítása, majd a layout és mobil navigáció migrálása.
4. A loading/error állapotok migrálása.
5. A lista- és részletoldal gridjének célzott migrálása.
6. A galéria React-Bootstrap modalra állítása.
7. A feleslegessé vált CSS eltávolítása és a megmaradó feature-stílusok rendezése.
8. Automatikus és vizuális ellenőrzés.

## Hatókörön kívül

- adminfelület vagy új üzleti feature létrehozása;
- teljes designváltás;
- külön globális state management;
- Bootstrap CDN vagy Bootstrap JavaScript bundle;
- minden React-Bootstrap komponens saját wrapperrel történő elfedése;
- end-to-end tesztframework bevezetése ebben a változtatásban.
