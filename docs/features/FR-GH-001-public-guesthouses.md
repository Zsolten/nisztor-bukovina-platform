# FR-GH-001 - Két panzió megjelenítése

## Hatókör

Ez a szelet a Nisztor Panzió és a Bukovina Panzió publikus listázását, külön részletútvonalát, egymástól független adatmodelljét és törlés nélküli inaktiválását valósítja meg. A kezdőadatok magyar szövegei és képei a felhasználó által jóváhagyott, 2026-08-04-én elért [meglévő panzióoldalról](https://www.nisztorpanzio.ro/) származnak.

Foglalás, e-mail, adminisztráció, árkezelés, időszakos árak, szolgáltatások és teljes szobatípusmodell nincs ebben a szeletben.

## Elfogadási feltételek leképezése

| Elfogadási feltétel | Megvalósítás | Ellenőrzés |
| --- | --- | --- |
| Mindkét panzió megjelenik a publikus oldalon. | `GET /api/guesthouses` csak aktív rekordokat ad, a frontend két külön kártyát jelenít meg. | backend lista integration teszt; frontend route teszt |
| Mindkét panzió részletes oldala megnyitható. | Nyelvi útvonal: `/{lang}/guesthouses/{slug}`, API: `GET /api/guesthouses/{slug}`. | backend részlet integration teszt; frontend részletútvonal-teszt |
| Az adatok egymástól függetlenül módosíthatók. | A két panzió külön `guesthouse` rekord, saját fordítás- és képrekordokkal. | Flyway séma és seed; repository-lekérdezések |
| Inaktívvá tehető adatvesztés nélkül. | A `guesthouse.active` jelző kizárja a rekordot a publikus lista- és részletlekérdezésből, törlés nélkül. | backend inaktiválási integration teszt |

## Adat- és képkezelés

A hivatalos galéria 2026-08-04-én elérhető képfájljai változtatás nélkül a `frontend/public/images/guesthouses/{panzio}` könyvtárakban vannak: 26 Nisztor- és 34 Bukovina-kép. Az adatbázis panziónként csak a relatív publikus útvonalat, alternatív szöveget, sorrendet és borítóképjelzőt tárolja. A tartalom magyarul seedelt; a `hu`, `ro` és `en` API-kérés magyar tartalomra esik vissza, amíg hiteles fordítás nem kerül az adatbázisba.

## Reprodukálható ellenőrzés

Frontend:

```bash
cd frontend
npm run format:check
npm run lint
npm run test
npm run build
```

Backend, Java 21 és futó Docker mellett:

```bash
cd backend
./gradlew check
```

Kézi ellenőrzéshez indítsd a PostgreSQL-t, a backendet és a frontendet, majd nyisd meg a `/hu`, `/hu/guesthouses/nisztor-panzio` és `/hu/guesthouses/bukovina-panzio` útvonalakat.
