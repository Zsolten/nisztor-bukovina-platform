# Frontend-architektúra

## Technológiai alap

A frontend React, TypeScript és Vite alapú alkalmazás. A routingot React Router, a háromnyelvű működést i18next és react-i18next támogatja. A reszponzív elrendezés és az általános UI-viselkedés alapja a React-Bootstrap; külön globális állapotkezelő nincs.

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
|   |-- styles/
|   `-- types/
|-- i18n/
|   |-- config.ts
|   |-- languages.ts
|   `-- resources.ts
|-- test/
|   `-- setup.ts
`-- main.tsx
```

Az üzleti képernyők és klienslogika a megfelelő feature alatt maradnak. A `shared` csak több feature által ténylegesen használt API-, komponens- és típusépítő elemeket tartalmaz. Üres feature könyvtár nem kerül verziókezelésbe.

## Megjelenés és UI-komponensek

A React-Bootstrap biztosítja a React-komponenseket, az akadálymentes interakciókat és a reszponzív rácsrendszert. Új navigáció, űrlap, modál, figyelmeztetés és általános reszponzív elrendezés elsődlegesen React-Bootstrap elemekből készüljön.

A Bootstrap megjelenése a `shared/styles` alatti Sass belépési ponton keresztül, a projekt saját szín-, tipográfiai és térköztokenjeivel épül. CDN-ről betöltött Bootstrap CSS, Bootstrap JavaScript és közvetlen DOM-alapú Bootstrap inicializálás nem használható.

Az oldal saját papír–erdőzöld–arany–téglavörös arculata és a feature-specifikus vizuális részletek alkalmazásstílusok maradnak. A Bootstrap a szerkezetet és a viselkedést adja, nem írja felül az arculatot. Külön wrapper komponens csak akkor készüljön, ha több helyen ismétlődő alkalmazásszintű viselkedést vagy vizuális szabályt foglal egységbe; egyszeri használatnál a React-Bootstrap komponens közvetlenül használható.

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
