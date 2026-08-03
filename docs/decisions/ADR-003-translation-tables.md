# ADR-003: Entitásonkénti fordítási táblák

## Állapot

accepted

## Kapcsolódó követelmények

- `FR-GH-003`
- `FR-TOUR-005`
- `FR-ADMIN-002`

## Kontextus

A panziók, szobatípusok, szolgáltatások, csillagtúrák és programpontok fordítható tartalma magyar, román és angol nyelven szerkeszthető. A többféle üzleti entitás eltérő szöveges mezőket és saját életciklust használ.

## Döntés

Minden többnyelvű üzleti entitás saját fordítási táblát kap. Az alapentitás a nyelvfüggetlen adatokat, az adott entitáshoz tartozó fordítási tábla pedig a fordítható szövegeket tárolja. Nem készül egyetlen általános, minden entitást és mezőt összemosó fordítási tábla.

## Következmények

- A fordítások sémája követi az adott üzleti entitás mezőit és élettartamát.
- Az adatbázis-korlátozások és lekérdezések entitásonként egyértelműen alakíthatók ki.
- Új fordítható entitás bevezetése saját fordítási séma és migráció létrehozását igényli.
- A konkrét táblák az érintett feature PR-jában készülnek el; a foundation nem hoz létre üzleti táblát.
