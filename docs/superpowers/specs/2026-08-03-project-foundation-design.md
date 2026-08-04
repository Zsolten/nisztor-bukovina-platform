# Project Foundation Design

- Dátum: 2026-08-03
- Állapot: jóváhagyott
- Branch: `chore/project-foundation`
- Normatív források: `docs/Requirements.pdf`, `docs/Product Vision and Goals.pdf`

## Cél

Az első pull request egy tiszta, futtatható, tesztelhető és dokumentált monorepo-alapot hoz létre. Nem valósít meg panzió-, szobatípus-, foglalási-, adminisztrációs vagy turisztikai üzleti funkciót.

A foundation merge-je után minden további fejlesztés kis, Requirements-azonosítóhoz kötött feature branchben és pull requestben készülhet, ugyanazon automatikus ellenőrzések mellett.

## Hatókör

Az első PR tartalma:

- a korábbi demo- és prototípuskód eltávolítása;
- tiszta Spring Boot backend;
- tiszta React frontend;
- PostgreSQL helyi fejlesztési környezet;
- Flyway migrációs alap;
- backend és frontend tesztkörnyezet;
- ArchUnit architektúrateszt-alap;
- háromnyelvű routing és i18n technikai alap;
- GitHub Actions CI;
- PR-sablon;
- architektúra-, döntés-, API- és nyomonkövetési dokumentáció.

## Hatókörön kívül

Az első PR nem tartalmaz:

- üzleti adatbázistáblát vagy seed adatot;
- publikus panzió- vagy szobatípus-végpontot;
- foglalási folyamatot vagy árkalkulációt;
- admin login működést;
- Redis- vagy RabbitMQ-integrációt;
- e-mail-küldést;
- AI-alapú útitervet;
- általános REST hibamodellt;
- UI frameworköt;
- automatikus deploymentet vagy konténer image buildet.

## Megőrzési és újraépítési határ

A korábbi alkalmazáskód nem része az új implementációnak.

Megmarad:

```text
backend/gradlew
backend/gradlew.bat
backend/gradle/wrapper/*
docs/Requirements.pdf
docs/Product Vision and Goals.pdf
```

Újra létrejön:

```text
backend/build.gradle
backend/settings.gradle
backend/src/*
frontend/*
compose.yaml
.env.example
.gitignore
.github/workflows/*
.github/pull_request_template.md
README.md
docs Markdown dokumentumok
```

## Monorepo szerkezet

```text
nisztor-bukovina-platform/
├── .github/
│   ├── workflows/ci.yml
│   └── pull_request_template.md
├── backend/
├── frontend/
├── docs/
│   ├── Requirements.pdf
│   ├── Product Vision and Goals.pdf
│   ├── architecture/
│   ├── decisions/
│   ├── api/openapi.yaml
│   ├── traceability/
│   └── superpowers/
├── .env.example
├── .gitignore
├── compose.yaml
└── README.md
```

## Backend design

### Technológia

```text
Java:          21
Spring Boot:   4.0.6
Gradle:        9.5.1 Wrapper, Groovy DSL
Alapcsomag:    com.bukovina.platform
Build típusa:  egyetlen Gradle projekt
```

Függőségek:

- Spring Web MVC;
- Spring Data JPA;
- Bean Validation;
- Spring Security;
- Actuator;
- Flyway és PostgreSQL támogatás;
- PostgreSQL JDBC driver;
- JUnit 5;
- Testcontainers PostgreSQL;
- ArchUnit;
- Spotless és Google Java Format;
- Checkstyle.

Spring Security jelen van, de a foundation fázisban minden HTTP-kérést explicit módon engedélyez. Adminhitelesítési mechanizmusról ez a design nem dönt; azt későbbi security ADR és feature PR határozza meg.

HTTP-n kizárólag az Actuator `health` végpontja van kiexponálva a management endpointok közül:

```text
GET /actuator/health
```

### Modulstruktúra

```text
com.bukovina.platform
├── accommodation
│   ├── guesthouse
│   ├── roomtype
│   ├── pricing
│   ├── amenity
│   └── booking
├── tourism
│   ├── activity
│   ├── startour
│   └── itinerary
├── support
│   ├── authentication
│   ├── administration
│   ├── translation
│   └── notification
├── shared
│   ├── configuration
│   ├── exception
│   └── validation
└── BukovinaPlatformApplication
```

A célmodulok `package-info.java` fájllal jönnek létre. Üres `controller`, `service`, `dao`, `model` és `dto` csomag nem készül. Egy technikai réteg akkor jelenik meg, amikor az első valódi osztálya elkészül.

### Modul- és rétegszabályok

Az ArchUnit alapja automatikusan ellenőrizhetővé teszi a következő irányelveket:

- controller nem használhat közvetlenül DAO-t;
- más modul nem férhet hozzá közvetlenül egy modul DAO-jához;
- más modul JPA entitása nem használható közvetlen modulközi szerződésként;
- `shared` nem függhet üzleti modultól;
- `accommodation` és `tourism` között nincs közvetlen függés;
- körkörös csomagfüggőség nem megengedett.

A foundation idején csak olyan szabály kerül aktív tesztbe, amely az üres modulváz mellett is értelmesen ellenőrizhető. A rétegszabályok a megfelelő rétegek első megjelenésekor kapnak bizonyító tesztet.

### Konfiguráció és adatbázis

A backend `application.yaml` fájlt használ, és a monorepo gyökerében található `.env` fájlt tölti be:

```yaml
spring:
  config:
    import: optional:file:../.env[.properties]
```

Az adatbázis-sémát Flyway verziózza. A foundation nem hoz létre üres vagy mesterséges üzleti migrációt. Az első migráció az első valódi adatmodellt megvalósító PR-ban készül.

A context teszt Testcontainers használatával valódi PostgreSQL 16 adatbázison indítja el az alkalmazást, így ellenőrzi a JPA-, datasource- és Flyway-konfiguráció együttműködését.

## Frontend design

### Technológia

```text
Node.js:    24.18.0 LTS
npm:        11.16.0
React:      19.2.6
TypeScript: 6.0.2
Vite:       8.0.12
```

További alapkönyvtárak:

- React Router;
- i18next és react-i18next;
- Vitest;
- React Testing Library;
- ESLint;
- Prettier.

UI komponenskönyvtár és külön globális állapotkezelő nem kerül a foundationbe.

### Forrásstruktúra

```text
src/
├── app/
│   ├── App.tsx
│   ├── router.tsx
│   └── providers.tsx
├── features/
│   ├── accommodation/
│   ├── tourism/
│   └── administration/
├── shared/
│   ├── api/
│   ├── components/
│   └── types/
├── i18n/
│   ├── config.ts
│   ├── languages.ts
│   └── resources.ts
├── test/
│   └── setup.ts
├── main.tsx
└── styles.css
```

Üres feature almappák csak akkor kerülnek verziókezelésbe, ha dokumentáló fájlt vagy valódi forrást tartalmaznak.

### Routing és nyelvkezelés

- Támogatott nyelvek: `hu`, `ro`, `en`.
- Alapértelmezett nyelv: `hu`.
- A kiválasztott nyelv `preferredLanguage` localStorage kulcsban tárolható.
- A `/` a tárolt támogatott nyelvre irányít; ennek hiányában `/hu`-ra.
- Nem támogatott nyelvkód, például `/de`, `/hu`-ra irányít.
- A foundation egyetlen minimális smoke screenje a `Nisztor-Bukovina Platform` címet jeleníti meg.
- Nincs publikus marketing- vagy üzleti oldal.

A frontend API-hívások relatív `/api` útvonalat használnak. Fejlesztéskor a Vite proxy a `http://localhost:8080` backend felé továbbítja ezeket. Későbbi production környezetben ugyanezt a relatív útvonalat Nginx szolgálhatja ki, így nem szükséges frontendoldali, környezetfüggő backend URL.

## Helyi infrastruktúra

A foundation Docker Compose fájlja csak PostgreSQL-t indít:

```text
Image:           postgres:16-alpine
Service:         postgres
Container:       bukovina-platform-postgres
Host port:       15432
Container port:  5432
Database:        bukovina_platform
User:            bukovina_app
Healthcheck:     pg_isready
```

Redis csak a rate limiting feature PR-jában, RabbitMQ csak a P2 notification fejlesztésben kerül be.

### Környezeti változók

A verziókezelt `.env.example` dokumentálja legalább:

```text
SERVER_PORT=8080
POSTGRES_DB=bukovina_platform
POSTGRES_USER=bukovina_app
POSTGRES_PASSWORD=
POSTGRES_HOST_PORT=15432
DB_URL=jdbc:postgresql://127.0.0.1:15432/bukovina_platform
DB_USERNAME=bukovina_app
DB_PASSWORD=
```

A valódi `.env` Gitből kizárt. Jelszóhoz nincs forráskódba írt alapértelmezés. Hiányzó kötelező jelszó esetén a Compose vagy az alkalmazás egyértelmű konfigurációs hibával áll le.

## API-szerződés

A fejlesztés contract-first megközelítést használ:

1. a kapcsolódó Requirements-azonosító alapján módosul az `openapi.yaml`;
2. elkészül a backend endpoint;
3. elkészül vagy frissül a frontend kliens;
4. integration és contract teszt bizonyítja az egyezést.

A foundation egy szintaktikailag érvényes, üzleti endpoint nélküli OpenAPI alapdokumentumot tartalmaz. Az adminhitelesítés token- vagy sessionmechanizmusát nem rögzíti.

## Minőségellenőrzés

### Backend

```text
./gradlew check
```

Ez futtatja:

- a fordítást;
- a JUnit teszteket;
- a Testcontainers context tesztet;
- az ArchUnit teszteket;
- a Checkstyle ellenőrzést;
- a Spotless formázási ellenőrzést.

### Frontend

```text
npm ci
npm run format:check
npm run lint
npm run test
npm run build
```

A frontend smoke teszt ellenőrzi a renderelést, a támogatott nyelvi útvonalat, a hibás nyelv átirányítását és a megjegyzett nyelv használatát.

## CI

A `.github/workflows/ci.yml` minden pull requestnél és a `main` branchre történő pushnál két párhuzamos jobot futtat.

### `backend-check`

- Java 21;
- Gradle cache;
- `./gradlew check`;
- Testcontainers a GitHub Actions Docker környezetén.

### `frontend-check`

- Node 24;
- npm cache;
- `npm ci`;
- formázási ellenőrzés;
- ESLint;
- Vitest;
- production build.

## Dokumentáció

A két PDF normatív forrás változatlanul megmarad. A Markdown dokumentáció ezek technikai leképezése, és nem írhatja felül őket.

Újra létrejön:

- dokumentációs index;
- rendszer-, backend-, frontend-, adat-, infrastruktúra- és security architektúra;
- ADR-ek a moduláris monolitról, üzleti modulhatárokról, translation táblákról, RoomType modellről, Redis rate limitingről és RabbitMQ notification eseményekről;
- követelmény-nyomonkövetési mátrix;
- contract-first OpenAPI alap;
- gyökér README helyi indítási és ellenőrzési parancsokkal.

A dokumentáció magyar, a kód, package-ek, API-nevek, commitok és branchnevek angol nyelvűek.

## Git és PR követhetőség

Elnevezési minta:

```text
Branch: feature/fr-gh-001-guesthouse-list
PR:     [FR-GH-001] Add public guesthouse listing
Commit: feat(guesthouse): add guesthouse query service
```

A foundation branch neve `chore/project-foundation`.

A PR-sablon mezői:

- Requirement ID;
- scope;
- acceptance criteria;
- test evidence;
- database migration;
- security/privacy impact;
- screenshots UI-változásnál;
- out of scope.

Issue- vagy Jira-sablon nem készül, amíg a backlogkezelő rendszer nincs kiválasztva.

## Hibakezelés és hibás környezet

- Hiányzó adatbázis-jelszó esetén nincs csendes alapérték.
- Elérhetetlen PostgreSQL esetén a backend indulása sikertelen és a hiba látható.
- Hibás vagy hiányzó localStorage nyelv esetén a frontend `/hu`-ra irányít.
- Nem támogatott nyelvi útvonal `/hu`-ra irányít.
- REST üzleti hibamodell csak az első valódi API-val együtt készül.

## Elfogadási feltételek

A foundation akkor kész, ha:

1. A repóban nincs korábbi demo- vagy prototípus-forrás.
2. A dokumentációból egyértelmű a két fő üzleti terület és a támogató modulok határa.
3. A PostgreSQL a gyökérből indítható és healthcheckje zöld.
4. A backend Java 21-gyel lefordul és PostgreSQL 16 Testcontainerrel sikeresen indul.
5. A `/actuator/health` sikeres választ ad futó adatbázis mellett.
6. A Spring Security konfiguráció minden alkalmazásútvonalat ideiglenesen engedélyez.
7. A frontend Node 24 LTS környezetben telepíthető, tesztelhető és buildelhető.
8. A `/`, támogatott nyelv és hibás nyelv routingja a jóváhagyott szabály szerint működik.
9. A backend és frontend formázási és lint ellenőrzése zöld.
10. A GitHub Actions backend és frontend jobja zöld.
11. A README egy új fejlesztő számára elegendő helyi indítási és ellenőrzési lépést tartalmaz.
12. A PR nem tartalmaz üzleti funkcionalitást vagy üzleti adatbázistáblát.
