# Nisztor-Bukovina Platform

Ez a repository a Nisztor-Bukovina Platform technikai foundationje. A foundation a fejlesztési, minőségi és üzemeltetési alapokat tartalmazza, üzleti funkciót nem: nincs üzleti végpont, üzleti adatbázistábla, seed adat vagy felhasználói folyamat.

## Üzleti területek

- **Szálláshely (`accommodation`)**: a két bukovinai panzió bemutatása, szobatípusok, árképzés, szolgáltatások és foglalási kérelmek későbbi üzleti területe.
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

## Követelményekhez kötött munka

Ez a foundation PR a `N/A - technical foundation` követelményértéket használja. Minden későbbi feature branchnek és pull requestnek legalább egy valós `FR-*` vagy `NFR-*` azonosítóra kell hivatkoznia. A branch és a PR címe is tartalmazza az azonosítót, például `feature/fr-gh-001-guesthouse-list` és `[FR-GH-001] Add public guesthouse listing`; a PR-ban a hatókört, elfogadási feltételeket, tesztbizonyítékot, valamint az adatbázis-, biztonsági és adatvédelmi hatást is rögzíteni kell.
