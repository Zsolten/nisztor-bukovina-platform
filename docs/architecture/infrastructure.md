# Infrastruktúra

## PostgreSQL a foundationben

A foundation egyetlen helyi infrastruktúra-szolgáltatása a PostgreSQL 16. A gyökér `compose.yaml` a `postgres:16-alpine` image-et használja, a szolgáltatás neve `postgres`, a konténer neve `bukovina-platform-postgres`, a host port `15432`, a konténerport `5432`. Az adatbázis neve `bukovina_platform`, az alkalmazásfelhasználó `bukovina_app`, az állapotot `pg_isready` healthcheck jelzi.

A jelszóhoz nincs forráskódba írt alapérték. A valódi `.env` nem verziókezelt; a `.env.example` csak a szükséges változókat dokumentálja. Hiányzó kötelező jelszó vagy elérhetetlen adatbázis esetén az indítás látható konfigurációs hibával sikertelen.

## Redis

Redis kizárólag a `NFR-SEC-001` és `NFR-SEC-004` szerinti rate limiting technikai támogatására tervezett. Nem általános gyorsítótár, nem session store, és nem része a foundationnek. Bevezetése a rate limiting feature PR feladata; az `ADR-005` állapota `planned`.

## RabbitMQ

RabbitMQ kizárólag a notification folyamatok későbbi üzenetközvetítője lehet. Nem része a foundationnek, és a Requirements.pdf prioritása szerint P2 integráció. A P1 e-mail-követelmények nem teszik a RabbitMQ-t P1 függőséggé. Az `ADR-006` állapota `deferred to P2`.

## Környezeti határok

| Környezet | Adatbázis és szolgáltatások | Határ |
| --- | --- | --- |
| Helyi fejlesztés | A gyökér Compose csak PostgreSQL-t indít. A frontend `/api` kéréseit a Vite proxy a helyi backendhez továbbítja. | Redis, RabbitMQ és üzleti seed adat nincs a foundationben. |
| Automatikus teszt | A backend context teszt Testcontainers által indított PostgreSQL 16 példányt használ. | A teszt nem függ a fejlesztő helyi Compose példányától. |
| Production | A frontend ugyanazt a relatív `/api` szerződést használja; ezt a későbbi környezetben Nginx szolgálhatja ki. | A foundation nem határoz meg automatikus deploymentet vagy konténer image buildet. |

Az infrastruktúra bővítése mindig az érintett `FR-*` vagy `NFR-*` azonosítóhoz, az OpenAPI-változáshoz, a konfiguráció dokumentálásához és az automatikus tesztekhez kötött.
