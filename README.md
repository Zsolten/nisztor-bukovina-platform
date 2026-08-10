# Nisztor-Bukovina Platform

Ez a repository a Nisztor-Bukovina Platform alkalmazását tartalmazza. A technikai foundation mellett elkészült az első üzleti szelet, az `FR-GH-001`: a publikus oldalon külön listázható és megnyitható a Nisztor Panzió és a Bukovina Panzió. A két panzió tartalma, képei és aktív állapota külön adatként kezelhető.

## Üzleti területek

- **Szálláshely (`accommodation`)**: a két bukovinai panzió publikus bemutatása már elérhető; a szobatípusok, árképzés, szolgáltatások és foglalási kérelmek későbbi üzleti szeletek.
- **Turizmus (`tourism`)**: a panzióktól független csillagtúrák, programpontok és tevékenységek későbbi üzleti területe.

## Előfeltételek

- Java 21
- Node.js 24.18.0 LTS
- npm 11.16.0
- Docker Desktop with Docker Compose

## Első beállítás

1. Másold a `.env.example` fájlt `.env` néven:

   ```powershell
   copy .env.example .env
   ```

2. A `.env` fájlban állítsd a `POSTGRES_PASSWORD` és a `DB_PASSWORD` értékét ugyanarra a helyi értékre.

3. Az admin API futtatásához adj meg egy legalább 32 bájtos, Base64-kódolt `ADMIN_JWT_SECRET` értéket. Fejlesztői admin fiókhoz töltsd ki az `ADMIN_BOOTSTRAP_EMAIL` és `ADMIN_BOOTSTRAP_PASSWORD` mezőket, majd indítsd a backendet `SPRING_PROFILES_ACTIVE=dev` profillal. Ez a bootstrap csak új e-mail-címhez hoz létre admin fiókot.

## Adatbázis

```powershell
docker compose up -d postgres
docker compose ps
```

A PostgreSQL inicializálási környezeti változói csak friss volume esetén hoznak létre szerepköröket és adatbázisokat. A foundation előtti, meglévő volume-ot a projekt soha nem törli automatikusan; szükség esetén kifejezett migrációt vagy külön néven indított Compose-projektet használj.

## Backend Windows alatt

```powershell
cd backend
.\gradlew.bat bootRun
.\gradlew.bat check
```

## Frontend Windows alatt

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
npm.cmd run format:check
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

## Elérhetőségek

- frontend: http://localhost:5173
- backend health: http://localhost:8080/actuator/health
- panzió API: http://localhost:8080/api/guesthouses?lang=hu

## Megvalósított követelmény

- [`FR-GH-001` - Két panzió megjelenítése](docs/features/FR-GH-001-public-guesthouses.md)

## Követelményekhez kötött munka

Minden további feature branchnek és pull requestnek legalább egy valós `FR-*` vagy `NFR-*` azonosítóra kell hivatkoznia. A branch és a PR címe is tartalmazza az azonosítót, például `feature/fr-gh-002-guesthouse-details` és `[FR-GH-002] Complete guesthouse details`; a PR-ban a hatókört, elfogadási feltételeket, tesztbizonyítékot, valamint az adatbázis-, biztonsági és adatvédelmi hatást is rögzíteni kell.
