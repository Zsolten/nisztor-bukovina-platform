# Backend-architektúra

## Technológiai alap

A backend Java 21 és Spring Boot 4.0.6 alapú, egyetlen Gradle projektként épül. Az alapcsomag `com.bukovina.platform`. A perzisztencia PostgreSQL-re, Spring Data JPA-ra és Flywayre épül; a bemeneti ellenőrzést Bean Validation támogatja.

## Package-by-feature szerkezet

```text
com.bukovina.platform
|-- accommodation
|   |-- guesthouse
|   |-- roomtype
|   |-- pricing
|   |-- amenity
|   `-- booking
|-- tourism
|   |-- activity
|   |-- startour
|   `-- itinerary
|-- support
|   |-- authentication
|   |-- administration
|   |-- translation
|   `-- notification
|-- shared
|   |-- configuration
|   |-- exception
|   `-- validation
`-- BukovinaPlatformApplication
```

A csomagok üzleti képességet jelölnek. `controller`, `service`, `dao`, `model` és `dto` alcsomag csak akkor jelenik meg, amikor valódi osztály kerül bele.

## Rétegezés

Az üzleti kérés feldolgozási iránya `controller -> service -> dao`.

- A controller a HTTP-szerződést és a bemenet átadását kezeli.
- A service végzi az üzleti koordinációt és ismételten érvényesíti a szerveroldali szabályokat.
- A dao a modul perzisztencia-hozzáférését zárja egységbe.
- A controller nem hívhat közvetlenül DAO-t.
- Külső modul nem hívhatja közvetlenül egy másik modul DAO-ját.

## Modulfüggőségek

Az `accommodation` és a `tourism` egymástól független üzleti terület. Modulközi együttműködéshez stabil, nem JPA-entitás alapú szerződés szükséges. A `shared` csak általános technikai elemeket tartalmazhat, és nem függhet üzleti modultól. Körkörös csomagfüggőség nem megengedett.

## Tranzakciós stratégia

Az üzleti tranzakció határa a service réteghez tartozik. Egy modul a saját perzisztens állapotát a saját DAO-ján keresztül módosítja; controllerben nem lehet tranzakciós üzleti folyamat. Modulközi szerződésként JPA-entitás nem adható át. Az `FR-GH-001` lekérdezései read-only service-tranzakcióban állítják össze a publikus DTO-kat, és csak az aktív panziókat adják vissza.

## Tesztelési stratégia

- A JUnit 5 tesztek fedik az üzleti szabályokat, amikor azok megjelennek.
- A context és perzisztencia-integrációs tesztek Testcontainers által indított valódi PostgreSQL 16 adatbázist használnak, és együtt ellenőrzik a datasource-, JPA- és Flyway-konfigurációt.
- Az ArchUnit ellenőrzi a modul- és réteghatárokat; csak olyan szabály aktív, amelyhez a szükséges kódstruktúra már létezik.
- Az API-khoz integration és contract teszt bizonyítja az `openapi.yaml` és az implementáció egyezését.
- Az `NFR-TEST-*` követelményekhez tartozó automatikus tesztek az érintett feature-rel együtt készülnek el.
