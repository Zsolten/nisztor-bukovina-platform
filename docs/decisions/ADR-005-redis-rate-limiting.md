# ADR-005: Redis a rate limiting támogatására

## Állapot

planned

## Kapcsolódó követelmények

- `NFR-SEC-001`
- `NFR-SEC-004`

## Kontextus

A publikus foglalási endpointot IP-cím és e-mail-cím alapján, az adminisztrátori belépési kísérleteket pedig IP-cím és admin e-mail-cím alapján kell korlátozni. A korlátozásnak szerveroldalon kell működnie. A foundation helyi infrastruktúrája jelenleg kizárólag PostgreSQL-t tartalmaz.

## Döntés

Redis kizárólag a rate limiting technikai támogatására tervezett. Nem része a foundationnek; a rate limiting feature PR vezeti be a szükséges konfigurációval és automatikus tesztekkel. Redis nem kap általános gyorsítótár- vagy session store szerepet.

## Következmények

- A foundation Compose konfigurációja nem tartalmaz Redis szolgáltatást.
- A rate limit implementációjának bizonyítania kell a normatív limiteket és a HTTP 429 választ.
- Redis más célú felhasználása külön döntést igényel.
- Az adminhitelesítés konkrét mechanizmusáról ez az ADR nem dönt.
