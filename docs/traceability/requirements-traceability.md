# Követelmény-nyomonkövetés

## Cél

Ez a mátrix a normatív `Requirements.pdf` követelménycsoportjait a megvalósításért felelős területhez rendeli. Nem módosítja a követelmények tartalmát, prioritását vagy elfogadási feltételeit.

## Tulajdonosi mátrix

| Követelménycsoport | Tulajdonos |
| --- | --- |
| `FR-GH-*` | `accommodation.guesthouse` |
| `FR-ROOM-*` | `accommodation.roomtype` |
| `FR-PRICE-*` | `accommodation.pricing` |
| `FR-BOOK-*` | `accommodation.booking` |
| `FR-PAY-*` | `accommodation.booking` |
| `FR-SERVICE-*` | `accommodation.amenity` |
| `FR-TOUR-*` | `tourism.startour` + `tourism.activity` |
| `FR-FAV-*` | frontend tourism favorites |
| `FR-ALT-*` | `tourism.startour`, P2 |
| `FR-ADMIN-*` | `support.administration` + `support.authentication` |
| `FR-EMAIL-*` | `support.notification`, P1 |
| `NFR-SEC-*` | security infrastructure and affected modules |
| `NFR-PRIV-*` | átfogó biztonsági és adatvédelmi architektúra, valamint az érintett modulok |
| `NFR-TECH-*` | platformarchitektúra, valamint az érintett frontend-, backend-, adat- és infrastruktúra-területek |
| `NFR-TEST-*` | complete automated test suite |

Az owner megjelölés elsődleges felelősséget jelent, nem jogosítja fel a modult más modul belső DAO-jának vagy JPA-entitásának közvetlen használatára. Az átfogó felelősség nem írja felül a normatív követelményt, és nem módosítja a meghatározott modulhatárokat.

## Megvalósítási állapot

| Követelmény | Állapot | Bizonyíték |
| --- | --- | --- |
| `FR-GH-001` | Megvalósítva | publikus lista és két külön részletútvonal; külön `guesthouse`, fordítás- és képmetadatok; `active` alapú elrejtés; backend integration és frontend route tesztek |

A részletes elfogadási leképezést az [`FR-GH-001` feature-dokumentum](../features/FR-GH-001-public-guesthouses.md) tartalmazza. A `FR-GH-002` és a további panzió-, szoba-, szolgáltatás-, ár-, kapcsolattartási, foglalási és adminisztrációs követelmények ettől továbbra is külön hatókörök.

## Változtatási szabály

Minden feature ticket és pull request legalább egy `FR-*` vagy `NFR-*` azonosítóra hivatkozik. A változtatás leírása összeköti az implementációt a normatív elfogadási feltételekkel, felsorolja a tesztbizonyítékot, és jelzi az adatbázis-, biztonsági és adatvédelmi hatást.

Az API-t érintő fejlesztésnél először az `openapi.yaml` módosul a kapcsolódó követelmény alapján, majd a backend és a frontend implementáció, végül az integration és contract teszt. Egy alacsonyabb szintű dokumentum vagy kódváltozás sem írhatja felül a normatív PDF-eket.
