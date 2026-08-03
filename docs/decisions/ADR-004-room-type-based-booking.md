# ADR-004: Szobatípus-alapú foglalási kérelem

## Állapot

accepted

## Kapcsolódó követelmények

- `FR-ROOM-001`
- `FR-ROOM-002`
- `FR-ROOM-003`
- `FR-BOOK-004`

## Kontextus

A platform foglalási kérelmet vesz fel, nem valós idejű, konkrét szobát értékesítő foglalást. A látogató szobatípust és darabszámot választ, a konkrét kapacitást és szobakiosztást a panzió utólag ellenőrzi.

## Döntés

Az MVP adatmodellje `RoomType` egységeket kezel, és nem vezet be egyedileg foglalható fizikai `Room` entitást vagy szobaszámot. A foglalási kérelem szobatípusonkénti darabszámot rögzíthet. A férőhelyet a kiválasztott szobatípusok kapacitásából kell kiszámítani, és a backend ugyanazt a szabályt ismételten ellenőrzi.

## Következmények

- A látogató nem választhat konkrét szobát.
- Nincs valós idejű szobakészlet vagy automatikus szobakiosztás.
- A foglalási kérelem nem válik automatikusan visszaigazolt foglalássá.
- Konkrét fizikai szobák későbbi bevezetése P2 hatókörű, külön döntést és adatmodell-változást igényel.
