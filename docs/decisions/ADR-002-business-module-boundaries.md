# ADR-002: Üzleti modulhatárok

## Állapot

accepted

## Kapcsolódó követelmények

- `FR-GH-*`
- `FR-ROOM-*`
- `FR-PRICE-*`
- `FR-BOOK-*`
- `FR-SERVICE-*`
- `FR-TOUR-*`
- `NFR-TECH-004`

## Kontextus

A platform két, egymástól üzletileg elkülönülő területet kezel. A szálláshelyi működés a panziókhoz, szobatípusokhoz, árakhoz, szolgáltatásokhoz és foglalási kérelmekhez tartozik. A csillagtúra-katalógus önálló tartalmi rész, nem kapcsolódik kötelezően panzióhoz vagy foglaláshoz.

## Döntés

Két fő üzleti terület készül:

- `com.bukovina.platform.accommodation`: `guesthouse`, `roomtype`, `pricing`, `amenity`, `booking`;
- `com.bukovina.platform.tourism`: `activity`, `startour`, `itinerary`.

Az adminisztráció, hitelesítés, fordítás és értesítés a `com.bukovina.platform.support` alatt marad. Az `accommodation` és a `tourism` között nincs közvetlen függés. Egy modul DAO-ja vagy JPA-entitása nem válhat másik modul közvetlen szerződésévé.

## Következmények

- A foglalási folyamat nem teszi függővé a turisztikai katalógust, és fordítva.
- A modulközi együttműködéshez stabil, belső perzisztenciamodelltől független szerződés szükséges.
- A támogató modulok koordinálhatnak képességeket, de nem olvaszthatják össze a két üzleti terület modelljét.
- A `shared` csak üzleti területtől független technikai elemeket tartalmazhat.
