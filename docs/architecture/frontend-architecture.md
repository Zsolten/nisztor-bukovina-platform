# Frontend-architektúra

## Technológiai alap

A frontend React, TypeScript és Vite alapú alkalmazás. A routingot React Router, a háromnyelvű működést i18next és react-i18next támogatja. A foundation nem vezet be UI komponenskönyvtárat vagy külön globális állapotkezelőt.

## Feature szerkezet

```text
src/
|-- app/
|   |-- App.tsx
|   |-- router.tsx
|   `-- providers.tsx
|-- features/
|   |-- accommodation/
|   |-- tourism/
|   `-- administration/
|-- shared/
|   |-- api/
|   |-- components/
|   `-- types/
|-- i18n/
|   |-- config.ts
|   |-- languages.ts
|   `-- resources.ts
|-- test/
|   `-- setup.ts
|-- main.tsx
`-- styles.css
```

Az üzleti képernyők és klienslogika a megfelelő feature alatt maradnak. A `shared` csak több feature által ténylegesen használt API-, komponens- és típusépítő elemeket tartalmaz. Üres feature könyvtár nem kerül verziókezelésbe.

## Nyelvi routing

A támogatott nyelvek `hu`, `ro` és `en`, az alapnyelv `hu`. A `/` a `preferredLanguage` kulcsban tárolt támogatott nyelvre irányít, ennek hiányában `/hu`-ra. Nem támogatott nyelvkód szintén `/hu`-ra irányít. A nyelvi útvonal és a megjelenített fordítás egymással összhangban marad.

## localStorage korlátok

- A `preferredLanguage` kizárólag a támogatott nyelvi választást tárolhatja.
- A túrakedvencek kulcsa `favoriteStarTours`; JSON-értéke kizárólag túraazonosítókat tartalmazhat.
- A kedvenceknél metaadat, teljes túratartalom, foglalási adat, személyes vagy más érzékeny adat nem tárolható.
- Hibás kedvencadat esetén az alkalmazás üres listával indulhat, és nem omolhat össze.
- A kedvencek nem szinkronizálódnak másik böngészőre vagy eszközre, és a böngészőadatok törlésével elveszhetnek.
- A teljes kedvenclista törlése `FR-FAV-006` szerint P1 feladat.

Az `FR-FAV-002` ajánlott tárolási struktúrájában megjelenő metaadat és az `NFR-PRIV-001` P0 elfogadási feltétele között látszólagos feszültség van. A normatív specifikáció tisztázásáig a szigorúbb P0 adatvédelmi feltétel az irányadó, ezért a frontend csak túraazonosítókat tárol.

## API-szerződés

A frontend minden backendhíváshoz relatív `/api` útvonalat használ. Helyi fejlesztéskor a Vite proxy a `http://localhost:8080` backendhez továbbítja a kéréseket; production környezetben ugyanaz a relatív szerződés használható. Környezetfüggő, forráskódba írt backend URL nem szükséges.

Az API-fejlesztés contract-first: először az `openapi.yaml` módosul a kapcsolódó `FR-*` vagy `NFR-*` azonosító alapján, majd a backend és a frontend implementáció, végül a contract teszt.
