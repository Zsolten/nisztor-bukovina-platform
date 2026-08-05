# Dokumentációs index

Ez a könyvtár a Nisztor-Bukovina Platform normatív termékforrásait és azok technikai leképezését tartalmazza.

## Irányadó sorrend

1. A Requirements.pdf a normatív követelményforrás.
2. A Product Vision and Goals.pdf a normatív termékcél-forrás.
3. Az architektúradokumentumok leírják a követelmények technikai megvalósítását.
4. Az ADR-ek indokolják a jelentős technikai döntéseket.
5. Az openapi.yaml a frontend és a backend API-szerződése.
6. Alacsonyabb szintű dokumentum egyik normatív PDF-et sem írhatja felül.

## Normatív források

- [Requirements.pdf](Requirements.pdf)
- [Product Vision and Goals.pdf](Product%20Vision%20and%20Goals.pdf)

## Technikai dokumentumok

- [Rendszerarchitektúra](architecture/system-architecture.md)
- [Backend-architektúra](architecture/backend-architecture.md)
- [Frontend-architektúra](architecture/frontend-architecture.md)
- [Adatmodell](architecture/data-model.md)
- [Infrastruktúra](architecture/infrastructure.md)
- [Biztonság](architecture/security.md)
- [ADR-001: Moduláris monolit](decisions/ADR-001-modular-monolith.md)
- [ADR-002: Üzleti modulhatárok](decisions/ADR-002-business-module-boundaries.md)
- [ADR-003: Entitásonkénti fordítási táblák](decisions/ADR-003-translation-tables.md)
- [ADR-004: Szobatípus-alapú foglalási kérelem](decisions/ADR-004-room-type-based-booking.md)
- [ADR-005: Redis a rate limiting támogatására](decisions/ADR-005-redis-rate-limiting.md)
- [ADR-006: RabbitMQ a P2 értesítésekhez](decisions/ADR-006-rabbitmq-notifications.md)
- [OpenAPI-szerződés](api/openapi.yaml)
- [Követelmény-nyomonkövetés](traceability/requirements-traceability.md)
- [`FR-GH-001` megvalósítás](features/FR-GH-001-public-guesthouses.md)
- [`FR-GH-002` publikus részletes tartalom](features/FR-GH-002-public-detail-content.md)

## Követelmény-hivatkozás

Minden feature ticketnek és minden pull requestnek legalább egy `FR-*` vagy `NFR-*` azonosítóra kell hivatkoznia. A hivatkozás mellett a hatókört, az elfogadási feltételeket és a tesztbizonyítékot is a normatív követelményhez kell kötni.
