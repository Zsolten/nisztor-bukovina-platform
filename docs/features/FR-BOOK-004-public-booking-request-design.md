# FR-BOOK-004 – Publikus foglalási kérelem: teljes tervezési specifikáció

## 1. Dokumentum célja és státusza

Ez a dokumentum a Nisztor–Bukovina Platform publikus foglalási folyamatának egységes termék-, UX-, UI- és technikai specifikációja. Összefoglalja a tervezés során elfogadott elrendezéseket, üzleti szabályokat, árképzést, validációkat, modulhatárokat, API- és adatmodell-elvárásokat, valamint a megvalósítási sorrendet.

Állapot: **tervezett, megvalósításra kész**, azzal a kivétellel, hogy a szobatípusonkénti tényleges darabszámokat még mindkét panzióhoz rögzíteni kell.

Ez a dokumentum nem írhatja felül a `Requirements.pdf`, a `Product Vision and Goals.pdf`, az elfogadott ADR-ek vagy a biztonsági követelmények szigorúbb előírásait. Eltérés esetén a normatív forrás az irányadó.

Kapcsolódó követelmény- és döntésazonosítók:

- `FR-BOOK-*`
- `FR-ROOM-*`
- `FR-PRICE-*`
- `FR-GH-002`
- `NFR-PRIV-001`
- `NFR-SEC-001`
- `ADR-001`: moduláris monolit
- `ADR-002`: üzleti modulhatárok
- `ADR-003`: entitásonkénti fordítási táblák
- `ADR-004`: szobatípus-alapú foglalási kérelem

## 2. Rögzített termékdöntések

### 2.1. A folyamat típusa

- A publikus látogató **bejelentkezés és regisztráció nélkül** küldhet foglalási kérelmet.
- A rendszer foglalási kérelmet vesz fel, nem valós idejű, automatikusan visszaigazolt foglalást értékesít.
- A beküldött kérelem kezdeti állapota `PENDING`.
- A felület minden releváns ponton jelzi: **„Ez még nem végleges foglalás. A szállásadó visszaigazolja az igényt.”**
- A bejelentkezés kizárólag a későbbi tulajdonosi/adminisztrációs funkciókhoz szükséges.
- A publikus folyamat nem kér és nem kínál vendégfiókot.

### 2.2. Nincs valós idejű availability

- A rendszer az MVP-ben nem vizsgál valós idejű szabad kapacitást.
- Nem jelenhet meg „Keresés”, „Szabad”, „Elérhető”, „Már csak N szoba”, „Azonnali visszaigazolás” vagy hasonló állítás.
- Nem készül konkrét fizikai `Room` entitás, szobaszám vagy automatikus szobakiosztás.
- A vendég szobatípust és darabszámot választ.
- A backend csak a fizikailag lehetetlen összeállításokat tiltja, például ha a kérés több szobát igényel, mint amennyi az adott típusból összesen létezik.
- A teljes készlet ellenőrzése nem jelent dátum szerinti availability-ellenőrzést.
- A végső kapacitást és szobakiosztást a szállásadó a kérelem beérkezése után ellenőrzi.

### 2.3. Képernyőfolyamat

A publikus foglalási folyamat javasolt lépései:

1. Időpont és vendégek száma.
2. Panzió és szobatípusok darabszámának kiválasztása.
3. Reggeli- és vacsoraigény megadása.
4. Kapcsolattartási és vendégadatok megadása.
5. Összegzés és a foglalási kérelem elküldése.
6. Beküldési visszaigazolás publikus hivatkozási azonosítóval.

A szobatípus- és étkezésválasztás ugyanazon a fő összeállító képernyőn jelenhet meg. A lépések technikailag lehetnek külön útvonalak vagy egy route-on belüli állapotok, de a felhasználó számára egyetlen folytonos folyamatot kell alkotniuk.

## 3. Navigáció és útvonalak

### 3.1. Meglévő fejléc

- A jelenlegi, `LanguageLayout` által biztosított fejlécet változtatás nélkül kell megtartani.
- A foglalási oldal nem vezet be külön mockup-fejlécet, új logókezelést vagy új elsődleges navigációt.
- A meglévő HU/RO/EN nyelvváltó, mobil offcanvas működés és görgetési állapot megmarad.
- A jelenlegi fejléc vizuális és viselkedési módosításai nem részei ennek a feature-nek.
- A foglalási oldal a közös layout részeként automatikusan megkapja a fejlécet és a láblécet.

### 3.2. Javasolt publikus route

```text
/:lang/guesthouses/:slug/booking
```

Példák:

```text
/hu/guesthouses/bukovina-panzio/booking
/ro/guesthouses/nisztor-panzio/booking
/en/guesthouses/bukovina-panzio/booking
```

- A panzió részletoldalán lévő jelenlegi foglalási placeholder gomb valódi linkké válik.
- A nyelvváltás ugyanazon foglalási útvonal és állapot logikai megfelelőjén marad.
- A publikus URL nem tartalmazhat nevet, e-mail-címet, telefonszámot, megjegyzést vagy más személyes adatot.

## 4. Szobatípusok

### 4.1. Bukovina Panzió

Publikusan választható szobatípusok:

- egyágyas szoba;
- kétágyas szoba;
- háromágyas szoba;
- négyágyas szoba.

### 4.2. Nisztor Panzió

Publikusan választható szobatípusok:

- egyágyas szoba;
- kétágyas szoba;
- háromágyas szoba.

### 4.3. Nyitott készletadat

A következő értékeket megvalósítás előtt a tulajdonossal egyeztetni és rögzíteni kell:

| Panzió | Szobatípus | Tényleges darabszám |
| --- | --- | ---: |
| Bukovina | 1 ágyas | TBD |
| Bukovina | 2 ágyas | TBD |
| Bukovina | 3 ágyas | TBD |
| Bukovina | 4 ágyas | TBD |
| Nisztor | 1 ágyas | TBD |
| Nisztor | 2 ágyas | TBD |
| Nisztor | 3 ágyas | TBD |

Ezeket az értékeket nem szabad mockupból, a teljes szobaszámból vagy korábbi migrációból kikövetkeztetni.

## 5. Végleges árképzési szabályok

### 5.1. Szállás

| Szobatípus | Díj | Egység |
| --- | ---: | --- |
| Egyágyas szoba | 200 RON | fő / éjszaka |
| Kétágyas szoba | 130 RON | fő / éjszaka |
| Háromágyas szoba | 130 RON | fő / éjszaka |
| Négyágyas szoba | 130 RON | fő / éjszaka |

Szabályok:

- A vendégek személyenként fizetnek, nem szobánként.
- Az egyágyas szobában elhelyezett vendég díja 200 RON/fő/éjszaka.
- A két-, három- és négyágyas szobában elhelyezett vendégek díja 130 RON/fő/éjszaka.
- Többágyas szoba részleges kihasználása esetén nincs külön, százalékos „single supplement”; csak az egyágyas szobatípus használja a 200 RON-os díjat.
- A korábbi mockupokon szereplő 170 RON-os és szobánkénti próbaárak nem érvényesek.

### 5.2. Étkezések

| Étkezés | Díj | Egység |
| --- | ---: | --- |
| Reggeli | 45 RON | fő / nap |
| Vacsora | 75 RON | fő / nap |
| Ebéd | nincs | nem választható |

Szabályok:

- A szobatípusok sorában nem jelenik meg statikus „Reggelivel” szöveg.
- A reggeli és a vacsora két egymástól független kapcsolóval választható.
- Bekapcsolt kapcsoló mellett megjelenik a `Hány főre?` számláló.
- A számláló alapértéke az összes vendég száma.
- A vendég az étkezést az összlétszámnál kevesebb személyre is kérheti.
- A számláló megengedett tartománya `1..vendégek száma`, ha a kapcsoló aktív.
- Kikapcsolt állapotban a kapcsolódó személyszám `0`, a vezérlő nem szerkeszthető és a tétel ára `0 RON`.
- Ebédkapcsoló, ebédmező, teljes ellátás vagy ebédet tartalmazó csomag nem jelenhet meg.
- Az MVP számítása szerint a választott reggeli és vacsora az összes foglalt éjszakára vonatkozik.

### 5.3. Dátum- és éjszakaszámítás

- Az érkezés és távozás naptári dátum, időpont és időzóna nélkül.
- Az érkezés napja beleszámít, a távozás napja nem számít új éjszakának.
- `éjszakák = távozás dátuma - érkezés dátuma`.
- A távozás dátumának későbbinek kell lennie az érkezés dátumánál.
- Minden hivatalos árkalkulációt a backend végez `BigDecimal` használatával.
- A pénznem minden esetben `RON`.

### 5.4. Szállásdíj képlete

Definíciók:

- `singleRoomCount`: kiválasztott egyágyas szobák darabszáma;
- `guestCount`: vendégek teljes száma;
- `multiRoomGuests = guestCount - singleRoomCount`;
- `nights`: foglalt éjszakák száma.

```text
singleAccommodation = singleRoomCount × 200 RON × nights
multiAccommodation  = multiRoomGuests × 130 RON × nights
accommodationTotal  = singleAccommodation + multiAccommodation
```

Minden kiválasztott egyágyas szobához pontosan egy vendég tartozik.

### 5.5. Étkezési díj képlete

```text
breakfastTotal = breakfastGuestCount × 45 RON × nights
dinnerTotal    = dinnerGuestCount × 75 RON × nights
mealTotal      = breakfastTotal + dinnerTotal
```

### 5.6. Végösszeg

```text
grandTotal = accommodationTotal + breakfastTotal + dinnerTotal
```

A publikus összesítő nem számolhat régi csomagárból, szobánkénti árból vagy frontendbe égetett próbaértékből.

### 5.7. Számítási példák

#### Hét vendég, három éjszaka, egyágyas szoba nélkül

Kiválasztás:

- 2 × kétágyas szoba;
- 1 × háromágyas szoba;
- 7 vendég;
- 3 éjszaka.

```text
Szállás: 7 × 130 × 3 = 2 730 RON
```

Ha mind a hét vendég reggelit és vacsorát is kér:

```text
Reggeli: 7 × 45 × 3 = 945 RON
Vacsora: 7 × 75 × 3 = 1 575 RON
Összesen: 2 730 + 945 + 1 575 = 5 250 RON
```

#### Öt vendég, három éjszaka, egy egyágyas szobával

Kiválasztás:

- 1 × egyágyas szoba;
- 2 × kétágyas szoba;
- 5 vendég;
- 3 éjszaka;
- étkezés nélkül.

```text
Egyágyas rész: 1 × 200 × 3 = 600 RON
Többi vendég: 4 × 130 × 3 = 1 560 RON
Összesen: 2 160 RON
```

## 6. Kapacitási és beviteli szabályok

### 6.1. Szobakiválasztás

- Legalább egy szobát ki kell választani.
- A szobatípus darabszáma nem lehet negatív.
- A kiválasztott darabszám nem haladhatja meg a szobatípus teljes fizikai darabszámát.
- Minden kiválasztott szobába legalább egy vendégnek kell jutnia.
- A kiválasztott teljes férőhely nem lehet kisebb a vendégek számánál.
- Az egyágyas szobák száma nem lehet nagyobb a vendégek számánál.
- Ha többágyas szoba is ki van választva, az egyágyas szobákhoz rendelt vendégek levonása után legalább egy vendégnek kell jutnia minden kiválasztott többágyas szobába.
- A backend ugyanazokat a szabályokat újra ellenőrzi; a frontend validáció csak gyors visszajelzés.

Kapacitás:

```text
selectedCapacity = Σ(roomType.quantitySelected × roomType.standardOccupancy)
selectedRoomCount = Σ(roomType.quantitySelected)
```

Alapvalidáció:

```text
selectedRoomCount <= guestCount <= selectedCapacity
singleRoomCount <= guestCount
```

### 6.2. Dátumok

- Érkezés nem lehet a múltban.
- Távozás kötelező és későbbi az érkezésnél.
- A maximális tartózkodási időt csak normatív üzleti követelmény alapján szabad korlátozni; jelen dokumentum nem talál ki maximumot.

### 6.3. Vendégadatok

Minimálisan bekérendő:

- kapcsolattartó teljes neve;
- e-mail-cím;
- telefonszám;
- opcionális megjegyzés;
- adatkezelési tájékoztató elfogadása, ha azt a normatív követelmény előírja.

Nem kérhető:

- jelszó;
- vendégfiók létrehozása;
- fizetési kártyaadat;
- a foglalási kérelemhez nem szükséges személyes adat.

## 7. Vizuális és elrendezési specifikáció

### 7.1. Meglévő designrendszer

A foglalási oldal a jelenlegi weboldal vizuális nyelvét használja:

- meleg papír háttér: `#f4efe4` és `#fbf8f1`;
- szövegszín: `#27241f`;
- mély erdőzöld: `#193027`;
- erdőzöld: `#29493a`;
- téglavörös: `#a84930`;
- tompa arany: `#c8984c`;
- serif címbetű a meglévő `$serif` tokenből;
- sans-serif törzsszöveg a meglévő `$sans-serif` tokenből;
- vékony elválasztók;
- szögletes vagy csak enyhén kerekített elemek;
- visszafogott árnyékok;
- nagyvonalú térközök;
- valós panziófotók.

Nem vezethető be külön, generikus kék SaaS-stílus, új fontpár vagy a meglévő oldaltól idegen kártyarendszer.

### 7.2. Felső foglalási összefoglaló sáv

A meglévő webhelyfejléc alatt egy sötét erdőzöld, csak olvasható sáv jelenik meg.

Tartalma:

- kis címke: `Foglalási adatok`;
- `Érkezés — YYYY. mmm. DD.`;
- `Távozás — YYYY. mmm. DD.`;
- `Vendégek — N fő`.

Megkötések:

- nincs `Időpont módosítása` gomb;
- nincs keresés gomb;
- nincs availability állítás;
- nincs primer CTA ebben a sávban;
- az információcsoportokat térköz és vékony függőleges elválasztók rendezik;
- mobilon a sáv törhet több sorba, de vízszintes túlcsordulás nem lehet.

### 7.3. Fő képernyő

Főcím:

```text
Állítsa össze a foglalást
```

Másodlagos navigáció:

```text
← Másik szálláshely választása
```

Asztali elrendezés:

- bal oldalon a kiválasztott panzió egy nagy, valós fényképe;
- jobb oldalon a panzió neve és a választható szobatípusok listája;
- a két oldal egy közös, vizuálisan egységes felületet alkot;
- nincs kártya a kártyában;
- a szobatípusok sorait térköz és vékony elválasztó rendezi;
- a kép nem torzulhat és a rendelkezésre álló kerethez illeszkedő `object-fit: cover` viselkedést használ.

### 7.4. Szobatípus sor

Minden sor tartalmazza:

- lokalizált szobatípusnév;
- kapacitás, például `3 fő / szoba`;
- a vonatkozó személyenkénti ár;
- `Szobák száma` felirat;
- mínusz gomb;
- aktuális darabszám;
- plusz gomb.

Nem tartalmazza:

- `Reggelivel` szöveget;
- availability állítást;
- szobánkénti árat;
- konkrét szobaszámot;
- „már csak N szoba” sürgetést.

A mínusz és plusz gomb legalább 44 × 44 px interakciós területű. Nulla értéknél a mínusz gomb letiltott. A teljes fizikai készlet elérésekor a plusz gomb letiltott, de a felület nem állít dátum szerinti elérhetőséget.

### 7.5. Férőhely-visszajelzés

A szobatípusok után rövid, élő összegzés jelenik meg:

```text
7 vendég · 7 kiválasztott férőhely
```

- Érvényes összeállításnál pozitív ikon és semleges/zöld visszajelzés használható.
- Túl kevés férőhelynél egyértelmű hiba jelenik meg.
- Túl sok kiválasztott szobánál a rendszer jelzi, hogy minden szobába legalább egy vendég szükséges.
- A hiba nem csak színnel kommunikálható.

### 7.6. Étkezésválasztó

A szobatípus-lista után két külön sor vagy panel jelenik meg:

#### Reggeli

- cím: `Reggeli`;
- ár: `45 RON / fő / nap`;
- kapcsoló: be/ki;
- bekapcsolva: `Hány főre?` számláló;
- opcionális sorösszeg.

#### Vacsora

- cím: `Vacsora`;
- ár: `75 RON / fő / nap`;
- kapcsoló: be/ki;
- bekapcsolva: `Hány főre?` számláló;
- opcionális sorösszeg.

Ebéd sem vizuálisan, sem a kliensoldali állapotban, sem az API publikus szerződésében nem választható.

### 7.7. Alsó összesítő

Az asztali nézetben a fő tartalom alján hangsúlyos, de nyugodt összesítő sáv jelenik meg.

Kötelező tartalom:

- kiválasztott szobák száma;
- vendégek száma;
- éjszakák száma;
- szállásdíj tételesen;
- reggeli tétel, ha aktív;
- vacsora tétel, ha aktív;
- végösszeg `RON` pénznemben;
- primer gomb: `Tovább az adatokhoz`;
- figyelmeztetés: `Ez még nem végleges foglalás. A szállásadó visszaigazolja az igényt.`

Az összesítő nem takarhat ki űrlapmezőt vagy fókuszált vezérlőt. Sticky viselkedés csak akkor használható, ha mobilon és nagyított nézetben is hozzáférhető marad minden tartalom.

### 7.8. Reszponzív viselkedés

Asztali referenciafelület: 1440 × 1024.

Tablet:

- a kép és a szobatípus-lista egymás alá törhet;
- az összesítő legfeljebb két sorba rendezhető;
- minden vezérlő megtartja a legalább 44 px interakciós méretet.

Mobil:

- a meglévő mobilfejléc marad;
- a felső adatösszefoglaló több sorba törik;
- a szobatípusnév, ár és darabszámvezérlő függőlegesen rendezhető;
- az étkezési kapcsoló és személyszámláló külön sorba törhet;
- a végösszeg és a CTA teljes szélességű lehet;
- nem lehet vízszintes overflow;
- sem sticky elem, sem modal nem takarhatja el a billentyűzetfókuszt.

## 8. Interakciós és állapotkezelési szabályok

- A kliensoldali foglalási állapot egy erre dedikált feature-ben kezelendő.
- A személyes adatok nem kerülhetnek `localStorage` vagy `sessionStorage` tárolóba.
- Oldalfrissítés utáni helyreállítás P0-ban nem szükséges, ha nincs rá külön normatív követelmény.
- A darabszám- és étkezésváltozások után a kliens rövid késleltetéssel új ajánlatot kérhet a backendtől.
- Betöltés közben a korábbi összeg nem jelenhet meg új, biztos értékként; kapjon `frissítés alatt` állapotot.
- Sikertelen kalkuláció esetén az előző ár ne legyen beküldhető.
- A CTA csak érvényes kapacitás és sikeres backend-ajánlat után aktív.
- A frontend által mutatott összeg tájékoztató; a beküldéskor a backend újraszámolja és pillanatképként menti.
- A böngésző vissza gombja nem veszítheti el indokolatlanul a nem személyes kiválasztásokat ugyanazon folyamaton belül.

## 9. Backend modulhatárok

Az alkalmazás továbbra is szigorú moduláris monolit, egyetlen deployolható Spring Boot alkalmazással.

### 9.1. `accommodation.roomtype`

Felelőssége:

- a publikált szobatípusok;
- típusonkénti teljes fizikai darabszám;
- standard férőhely;
- panzióhoz tartozás;
- lokalizált megnevezés.

A modul DAO-ja nem szivároghat ki más modulba. A `booking` modul kizárólag publikus service/query szerződésen keresztül kérhet adatot.

### 9.2. `accommodation.pricing`

Felelőssége:

- aktív publikus ártételek;
- pénznem;
- egyágyas és többágyas személyár;
- reggeli- és vacsoraár;
- lokalizált címkék.

A hivatalos foglalási számítás szabályai a backendben vannak. A `booking` modul publikus pricing szerződésen keresztül kapja az aktuális árakat; közvetlenül nem használja a pricing DAO-t.

### 9.3. `accommodation.booking`

Felelőssége:

- ajánlatkalkuláció;
- kapacitási validáció;
- foglalási kérelem létrehozása;
- ár-pillanatkép mentése;
- állapotátmenetek;
- publikus foglalási azonosító;
- későbbi adminműveletekhez szükséges üzleti szolgáltatások.

A controller nem függhet közvetlenül DAO-tól.

## 10. Adatbázis és migráció

### 10.1. Meglévő migrációk kezelése

- A korábban lefutott `V1`, `V2` és `V3` migráció nem módosítható.
- A szobatípus- és árkorrekció új, előre mutató Flyway-migrációban készül.
- A migráció várható neve: `V4__align_booking_room_types_and_pricing.sql`.

### 10.2. Árlista korrekciója

Az új migráció:

- aktívan hagyja vagy beállítja az `accommodation = 130 RON/person_night` tételt;
- aktívan hagyja vagy beállítja a `single_occupancy_room = 200 RON/person_night` tételt;
- aktívan hagyja vagy beállítja a `breakfast = 45 RON/person` tételt;
- aktívan hagyja vagy beállítja a `dinner = 75 RON/person` tételt;
- deaktiválja a `lunch` tételt;
- deaktiválja a külön csomagként tárolt `bed_and_breakfast`, `half_board` és `full_board` tételeket a publikus foglalási számításból;
- megőrzi a HU/RO/EN fordítási struktúrát.

### 10.3. Tervezett foglalási táblák

#### `booking_request`

Javasolt mezők:

- `id UUID`;
- `public_reference` egyedi, nem szekvenciális publikus azonosító;
- `guesthouse_id`;
- `check_in`;
- `check_out`;
- `guest_count`;
- `breakfast_guest_count`;
- `dinner_guest_count`;
- `status`;
- `currency`;
- `quoted_total`;
- `contact_name`;
- `contact_email`;
- `contact_phone`;
- `guest_note` opcionálisan;
- `language_code`;
- `created_at`;
- `updated_at`.

#### `booking_room_selection`

Javasolt mezők:

- `booking_request_id`;
- `room_type_id`;
- `quantity`;
- `standard_occupancy_snapshot`;
- a kalkulációhoz szükséges árpillanatkép vagy kapcsolódó price line.

#### `booking_price_line`

Javasolt mezők:

- `booking_request_id`;
- `code`;
- `description`;
- `quantity`;
- `unit_amount`;
- `line_total`;
- `currency`.

Az ár-pillanatkép azért szükséges, hogy egy későbbi árváltozás ne írja át a korábban beküldött kérelem összegét.

## 11. API-szerződés

Az OpenAPI-dokumentum a megvalósítással együtt, additív módon frissítendő.

### 11.1. Ajánlatkérés

```http
POST /api/booking-quotes
```

Példa kérés:

```json
{
  "guesthouseSlug": "bukovina-panzio",
  "checkIn": "2026-08-21",
  "checkOut": "2026-08-24",
  "guestCount": 7,
  "rooms": [
    { "roomTypeId": "double", "quantity": 2 },
    { "roomTypeId": "triple", "quantity": 1 }
  ],
  "breakfastGuestCount": 7,
  "dinnerGuestCount": 0
}
```

Példa válasz:

```json
{
  "currency": "RON",
  "nights": 3,
  "guestCount": 7,
  "selectedRoomCount": 3,
  "selectedCapacity": 7,
  "lines": [
    {
      "code": "accommodation",
      "quantity": 21,
      "unitAmount": 130,
      "lineTotal": 2730
    },
    {
      "code": "breakfast",
      "quantity": 21,
      "unitAmount": 45,
      "lineTotal": 945
    }
  ],
  "total": 3675,
  "requestOnly": true
}
```

Az endpoint:

- nem ment foglalási kérelmet;
- nem állít rendelkezésre állást;
- nem foglal le kapacitást;
- minden árat a backendben számol;
- strukturált validációs hibát ad.

### 11.2. Foglalási kérelem beküldése

```http
POST /api/booking-requests
```

A kérés tartalmazza az ajánlatkérés adatait, valamint:

- kapcsolattartó neve;
- e-mail-címe;
- telefonszáma;
- opcionális megjegyzése;
- felület nyelve;
- szükséges adatkezelési elfogadás.

A backend beküldéskor újraszámolja az árat. A kliens által küldött végösszeg nem tekinthető hiteles forrásnak.

Sikeres válasz minimális tartalma:

```json
{
  "reference": "NB-...",
  "status": "PENDING",
  "requestOnly": true
}
```

## 12. Foglalási állapotok

Publikus beküldés:

```text
PENDING
```

Tervezett adminállapot-átmenetek:

```text
PENDING -> CONFIRMED
PENDING -> REJECTED
PENDING -> CANCELLED
CONFIRMED -> CANCELLED
CONFIRMED -> COMPLETED
```

- A publikus beküldés soha nem állíthat közvetlenül `CONFIRMED` állapotot.
- Az adminfelület és adminhitelesítés külön feature, de a booking domain állapotmodellje készüljön fel rá.
- Érvénytelen állapotátmenetet a backend elutasít.

## 13. Biztonság és adatvédelem

- A publikus GET és foglalási POST folyamat anonim.
- Az adminfunkciók később kizárólag hitelesített tulajdonos/admin számára érhetők el.
- A teljes foglalási kérés nem kerülhet alkalmazáslogba.
- E-mail-cím naplózása csak maszkolva engedélyezett.
- Ellenőrző vagy admin token nem naplózható.
- Foglalási és személyes adat nem kerülhet `localStorage` vagy `sessionStorage` tárolóba.
- A frontend hibakövetése nem küldheti el a teljes űrlapállapotot.
- A publikus foglalási endpoint szerveroldali rate limitje:
  - IP-címenként 5 kérés / 10 perc;
  - e-mail-címenként 3 kérés / óra;
  - IP-címenként 20 kérés / nap.
- Limit elérésekor HTTP `429` válasz szükséges.
- A CSRF-, CORS- és hitelesítési döntéseket a végleges security konfigurációban explicit módon kell kezelni; a jelenlegi `permitAll` foundation nem tekinthető kész production securitynek.

## 14. Lokalizáció

- A teljes folyamat HU/RO/EN nyelven elérhető.
- A route-ok nyelvi prefixe megmarad: `/hu`, `/ro`, `/en`.
- Minden felhasználói szöveg i18next erőforrásból érkezik.
- Backend lokalizált üzleti címkéknél a meglévő magyar fallback használható.
- A pénzformázás locale-függő, de a pénznem mindig `RON`.
- A dátumok megjelenítése locale-függő; az API ISO `YYYY-MM-DD` dátumot használ.
- A backend validációs hiba stabil hibakódot ad; a frontend fordítja a megjelenő szöveget.

## 15. Akadálymentesség

- Minden interaktív célterület legalább 44 × 44 px.
- A plusz/mínusz gomboknak egyértelmű hozzáférhető nevük van, például `Kétágyas szobák számának növelése`.
- A kapcsolók valódi checkbox/switch szemantikát használnak.
- A kapcsoló és a `Hány főre?` mező programozottan összekapcsolt.
- A hibaüzenetek mezőhöz kötöttek és nem csak színnel kommunikálnak.
- A végösszeg változása visszafogott `aria-live` régióban jelezhető.
- A fókuszsorrend követi a vizuális sorrendet.
- Billentyűzettel minden funkció elérhető.
- A meglévő `prefers-reduced-motion` viselkedést tiszteletben kell tartani.
- A desktop és mobil nézet legalább WCAG AA kontrasztot tartson.

## 16. Hibakezelés

Frontend állapotok:

- kezdeti;
- kalkuláció folyamatban;
- kalkuláció sikeres;
- validációs hiba;
- hálózati hiba;
- beküldés folyamatban;
- beküldés sikeres;
- beküldés sikertelen.

Szabályok:

- Hálózati hiba nem nullázhatja csendben a felhasználó nem személyes kiválasztásait.
- Sikertelen backend-kalkuláció után a beküldő CTA letiltott.
- Dupla kattintás nem hozhat létre két kérelmet; a beküldés idempotenciáját vagy deduplikációját backendoldalon kezelni kell.
- A publikus hibaüzenet nem fedhet fel stack trace-t, SQL-t, belső azonosítót vagy biztonsági részletet.
- Az ismeretlen panzió vagy szobatípus strukturált `404`/validációs hibát ad.
- Árváltozás esetén a backend friss ajánlatot ad vissza, és a felhasználónak újra jóvá kell hagynia az összeget.

## 17. Frontend komponensjavaslat

Javasolt feature-könyvtár:

```text
frontend/src/features/booking/
  BookingPage.tsx
  BookingContextSummary.tsx
  RoomTypeSelector.tsx
  RoomQuantityStepper.tsx
  CapacityFeedback.tsx
  MealOptions.tsx
  MealOptionRow.tsx
  BookingPriceSummary.tsx
  GuestDetailsForm.tsx
  BookingReview.tsx
  BookingConfirmation.tsx
  bookingReducer.ts
  bookingValidation.ts
  bookingApi.ts
```

Megkötések:

- A közös fejléc nem kerül át a feature-könyvtárba.
- Az üzleti végösszeg nem a React komponensekben készül.
- A kliensoldali kalkuláció legfeljebb előnézet; a backend válasza az irányadó.
- A komponensek ne hozzanak létre indokolatlan globális state-et.
- A szobaszámláló és étkezésszámláló önállóan tesztelhető legyen.

## 18. Backend komponensjavaslat

Javasolt szerkezet:

```text
backend/src/main/java/com/bukovina/platform/accommodation/booking/
  controller/
    PublicBookingQuoteController.java
    PublicBookingRequestController.java
  dto/
    BookingQuoteRequest.java
    BookingQuoteResponse.java
    CreateBookingRequest.java
    BookingRequestCreatedResponse.java
  service/
    BookingQuoteService.java
    BookingRequestService.java
    BookingPriceCalculator.java
  model/
    BookingRequest.java
    BookingRoomSelection.java
    BookingPriceLine.java
    BookingStatus.java
  dao/
    BookingRequestRepository.java
```

Megkötések:

- Controller nem használ DAO-t közvetlenül.
- A kalkulátor tiszta, determinisztikus és unit tesztelhető.
- A booking modul más modul perzisztenciaobjektumát nem használja szerződésként.
- A quote és create végpont ugyanazt a kalkulátort használja.
- A pénzügyi adat `BigDecimal`, nem `double`.
- A foglalási mentés és ár-pillanatkép egy tranzakcióban történik.

## 19. Megvalósítási sorrend

### 1. Adat- és szerződéskorrekció

- pontos szobatípus-darabszámok jóváhagyása;
- V4 migráció;
- pricing és room type query szerződések frissítése;
- OpenAPI quote szerződés.

### 2. Backend árkalkuláció

- tiszta kalkulátor;
- kapacitási validáció;
- quote endpoint;
- unit és integration tesztek.

### 3. Foglalási összeállító frontend

- route és placeholder link cseréje;
- meglévő fejléc változatlan használata;
- felső adatösszefoglaló;
- szobatípus-darabszámok;
- étkezési kapcsolók és személyszámok;
- backend-alapú árösszesítő;
- reszponzív és akadálymentes állapotok.

### 4. Vendégadatok és mentés

- booking táblák;
- create endpoint;
- vendégadat-űrlap;
- review és confirmation képernyő;
- idempotencia és rate limiting.

### 5. Teljes ellenőrzés

- backend check;
- frontend format, lint, test és build;
- valós PostgreSQL/Flyway indítás;
- manuális desktop, tablet és mobil ellenőrzés;
- HU/RO/EN ellenőrzés;
- billentyűzetes és képernyőolvasó-alapú alapellenőrzés.

## 20. Tesztelési minimum

### 20.1. Backend unit tesztek

- 7 vendég, 2 kétágyas + 1 háromágyas, 3 éjszaka: `2 730 RON` étkezés nélkül;
- ugyanaz 7 reggelivel: `3 675 RON`;
- ugyanaz 7 reggelivel és 7 vacsorával: `5 250 RON`;
- 1 egyágyas + 2 kétágyas, 5 vendég, 3 éjszaka: `2 160 RON`;
- részleges étkezési létszám;
- kevés férőhely;
- több kiválasztott szoba, mint vendég;
- szobatípus teljes készletének túllépése;
- nulla vagy negatív éjszaka;
- étkezési létszám nagyobb az összlétszámnál;
- nincs ebédtétel.

### 20.2. Backend integration tesztek

- publikus quote autentikáció nélkül;
- publikus booking create autentikáció nélkül;
- szerveroldali újraszámolás;
- `PENDING` kezdeti állapot;
- ár-pillanatkép mentése;
- rollback hibás mentésnél;
- Flyway upgrade meglévő V1–V3 adatbázisról;
- HU/RO/EN címkék és magyar fallback;
- rate limit `429` válasz.

### 20.3. Frontend tesztek

- a jelenlegi header változatlanul megjelenik;
- a felső sávban csak dátumok és vendégszám van, módosítási gomb nincs;
- szobatípusonkénti plusz/mínusz vezérlés;
- Bukovina 1–4 ágyas típusai;
- Nisztor 1–3 ágyas típusai;
- nincs `Reggelivel` szobasor-szöveg;
- külön reggeli- és vacsorakapcsoló;
- bekapcsoláskor személyszám megjelenik;
- ebéd nem jelenik meg;
- kapacitási visszajelzés;
- backend quote összegének megjelenítése;
- kérelemjelleg figyelmeztetése;
- inaktív CTA hibás állapotban;
- mobil overflow hiánya;
- billentyűzetes használat;
- HU/RO/EN fordítások.

### 20.4. Reprodukálható parancsok Windows alatt

Backend:

```powershell
cd backend
.\gradlew.bat test
.\gradlew.bat check
```

Frontend:

```powershell
cd frontend
npm.cmd run format:check
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

A compile/context teszt és a valós PostgreSQL-lel végzett `bootRun` külön ellenőrzési lépés.

## 21. Elfogadási feltételek

A feature akkor tekinthető késznek, ha:

1. A látogató autentikáció nélkül eléri a foglalási folyamatot.
2. A meglévő fejléc vizuálisan és funkcionálisan változatlan marad.
3. A felület sehol nem állít valós idejű availability-t.
4. A látogató a panzióhoz engedélyezett szobatípusokból darabszámot választ.
5. A rendszer a kiválasztott férőhelyet és a vendégszámot ellenőrzi.
6. Az egyágyas vendég 200 RON/fő/éj, a többi vendég 130 RON/fő/éj alapján számolódik.
7. A reggeli 45, a vacsora 75 RON/fő/nap; mindkettő külön kapcsolható és személyszámmal adható meg.
8. Ebéd sem a felületen, sem az ajánlat API-ban nem választható.
9. A hivatalos árat a backend számítja és a beküldéskor újraszámolja.
10. A vendég látja a tételes szállás-, reggeli-, vacsora- és végösszeget.
11. A felület jelzi, hogy a kérelem szállásadói visszaigazolást igényel.
12. A beküldött kérés `PENDING` állapotú és ár-pillanatképet tartalmaz.
13. Személyes adat nem kerül böngészőoldali tartós tárba vagy érzékeny logba.
14. A folyamat HU/RO/EN nyelven, desktopon és mobilon használható.
15. A backend, frontend, architektúra- és migrációs tesztek sikeresek.

## 22. Kifejezetten hatókörön kívül

- valós idejű availability;
- konkrét fizikai szobák és szobaszámok;
- automatikus szobakiosztás;
- automatikus foglalás-visszaigazolás;
- vendégregisztráció és vendégfiók;
- online fizetés és bankkártyaadat-kezelés;
- ebéd;
- teljes ellátás és régi csomagárak;
- adminfelület megvalósítása ebben a szeletben;
- adminhitelesítés megvalósítása ebben a szeletben;
- AI-alapú szobaajánlás;
- egyedi kedvezmény- vagy kuponmotor;
- dátum szerinti készletfoglalás.

## 23. Nyitott kérdés a megvalósítás előtt

Az egyetlen kötelezően tisztázandó üzleti adat:

> Pontosan hány darab 1, 2, 3 és 4 ágyas szoba van a Bukovina Panzióban, illetve hány darab 1, 2 és 3 ágyas szoba van a Nisztor Panzióban?

E nélkül a UI, a kalkulátor és az API elkészíthető, de a produkciós migráció és a maximális darabszám-validáció nem véglegesíthető.
