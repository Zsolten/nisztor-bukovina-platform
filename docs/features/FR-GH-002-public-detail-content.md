# FR-GH-002 - Publikus részletes panziótartalom

## Hatókör és állapot

Ez a szelet az `FR-GH-002` publikus információs részét valósítja meg a Nisztor Panzió és a Bukovina Panzió számára magyar, román és angol nyelven. A részletoldal leírást, történeti hátteret, lokalizált képleírásokat, szobatípusokat, szolgáltatásokat, árakat, elérhetőségeket és címet jelenít meg. A jóváhagyott árak publikálhatók; a tartalomforrás a `docs/content/guesthouses.hu-ro-en.yaml`.

A teljes `FR-GH-002` részben megvalósított. Foglalási navigáció és foglalási kérelem, adminisztrációs szerkesztés, valamint az adminmódosítások azonnali publikus megjelenése nem része ennek a szeletnek.

## Elfogadási feltételek leképezése

| Publikus információs feltétel | Megvalósítás | Automatizált bizonyíték |
| --- | --- | --- |
| Mindkét panzió teljes részletes tartalma elérhető. | A V2 Flyway migráció normalizált rekordokkal seedeli a történetet, képleírásokat, kapcsolatot, címet, szobatípusokat, szolgáltatásokat és árazást. | `GuesthouseContentMigrationTests`; `PublicGuesthouseControllerTests` |
| A tartalom HU/RO/EN nyelven jelenik meg. | Entitásonkénti fordítási táblák, kért nyelv és magyar fallback; a képek alternatív szövege is lokalizált. | teljes angol részlet- és magyar fallback integration teszt |
| Csak aktív publikus tartalom jelenik meg. | A panzió, szobatípus, szolgáltatás-hozzárendelés, ár, kapcsolat, cím és árazás aktív jelzőkkel szűrhető. | inaktív panzió- és inaktív részlettartalom integration tesztek |
| A szobák és szolgáltatások áttekinthetők. | Reszponzív szobakártyák és fix kategóriasorrendben megjelenő, nem interaktív szolgáltatás-tagek. | `GuesthouseDetailPage.test.tsx` |
| Az árak és feltételek egyértelműek. | Pénznemmel és egységgel megjelenő ártételek, felárak, kedvezmények és fizetési megjegyzés. | backend teljes részlet teszt; frontend `130 RON` és `1%` ellenőrzés |
| A kapcsolatfelvétel telefonon és e-mailben egyszerű. | Legalább 44 px magas `tel:` és `mailto:` linkek, lokalizált cím és koordináta-alapú térképlink. | frontend link- és címellenőrzés |
| A galéria asztali és mobil nézetben rendezett. | Az API-sorrendet megtartó, hatpozíciós aszimmetrikus minta; mobilon két váltakozó oszlop és teljes szélességű kiemelt képek; változatlan fényképező-modal. | `GuesthouseGallery.test.tsx` sorrend-, osztály- és ciklikus navigációs teszt |

## Adatmodell és modulhatárok

A V2 migráció a `guesthouse` modulhoz kapcsolódó fordítás-, kép-, kapcsolat- és címtáblákat, továbbá a `roomtype`, `amenity` és `pricing` modulok normalizált tábláit hozza létre. A publikus összeállítás modulonkénti `RoomTypeQuery`, `AmenityQuery` és `PricingQuery` szolgáltatási szerződéseket használ. Más modul DAO-ját vagy perzisztenciaobjektumát nem szivárogtatja ki; ezt ArchUnit szabályok védik.

Az API az `docs/api/openapi.yaml` szerződés additív `GuesthouseDetail` mezőit adja vissza. A frontend csak ezt a szerződést ismeri, a YAML szerkezetét nem.

## Nem szállított követelményrészek

- nincs foglalási gomb, útvonal vagy beküldés;
- nincs adminisztrációs tartalom- és árszerkesztés;
- nincs adminisztrációs módosításból eredő azonnali publikus frissítés;
- nincs új vendég-személyesadat-kezelés, fizetés vagy értesítés.

Ezek külön követelményszeletekben készülnek el; a jelenlegi publikus API és normalizált adatmodell ezekhez stabil alapot biztosít.

## Reprodukálható ellenőrzés

Backend, Java 21 és futó Docker mellett:

```bash
cd backend
./gradlew check
```

Frontend, Node 24 mellett:

```bash
cd frontend
npm run format:check
npm run lint
npm run test
npm run build
```
