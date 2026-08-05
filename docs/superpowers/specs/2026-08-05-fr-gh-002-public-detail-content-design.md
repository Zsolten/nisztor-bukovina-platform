# FR-GH-002 publikus részletes panziótartalom — design

## Állapot

Felhasználó által jóváhagyott design, implementációs tervre kész.

## Cél

A Nisztor Panzió és a Bukovina Panzió jelenlegi részletoldala a `docs/content/guesthouses.hu-ro-en.yaml` jóváhagyott tartalma alapján teljes, könnyen áttekinthető információs oldallá bővül. A látogató foglalás indítása nélkül megismeri a panzió történetét, szobatípusait, szolgáltatásait, árait, címét és kapcsolattartási adatait magyarul, románul vagy angolul.

A megjelenés megőrzi a jelenlegi papír–erdőzöld–arany–téglavörös arculatot. A fő cél a gyors információfelvétel, a mobilos olvashatóság és a két panzió közötti egyértelmű különbségtétel.

## Normatív és tartalmi források

- `docs/Requirements.pdf`, `FR-GH-002 – Panzió részletes bemutatása`;
- `docs/content/guesthouses.hu-ro-en.yaml` mint szerkesztési és seedforrás;
- `docs/decisions/ADR-002-business-module-boundaries.md`;
- `docs/decisions/ADR-004-room-type-based-booking.md`;
- a jelenlegi OpenAPI-szerződés, backendmodell és React-részletoldal;
- Baymard travel-accommodation UX kutatás: <https://baymard.com/blog/new-research-travel-accommodations>;
- W3C mobilos hozzáférhetőségi útmutató: <https://www.w3.org/TR/mobile-accessibility-mapping/>.

## Hatókör

### Ebben a szeletben elkészül

- a YAML árainak tulajdonosi megerősítése és publikálhatóvá tétele;
- mindkét panzió háromnyelvű rövid és részletes bemutatása;
- a bukovinai székely közösségi örökség külön történeti blokként;
- a publikus kapcsolattartási adatok és a cím/GPS-hely;
- strukturált szobatípusok, darabszámok, férőhelyek és pótágyadatok;
- csak a publikálható szolgáltatások, kategorizált címkékkel;
- a jóváhagyott nyilvános árlista, adó-, kedvezmény- és fizetési megjegyzésekkel;
- háromnyelvű képalternatív szövegek;
- kibővített, adatbázisból felépített publikus API;
- reszponzív, mobilon könnyen pásztázható részletoldal;
- contract-, backend integration- és frontend regressziós tesztek.

### Tudatosan nem készül el

- foglalási oldal, foglalási kérelem vagy foglalási CTA;
- vendégadatok bekérése vagy tárolása;
- adminisztrátori hitelesítés és adminfelület;
- admin CRUD API;
- valós idejű elérhetőség vagy konkrét szobakiosztás;
- térképbeágyazás vagy külső térképszolgáltató integráció;
- a nem megerősített kerékpárkölcsönzés és háziállat-megtekintés publikálása;
- ellenőrizetlen család- vagy panzióalapítási történet kitalálása.

Az `FR-GH-002` foglalási és adminisztrációs elfogadási feltételei ezért ebben a szeletben még nem tekinthetők teljesítettnek. A megvalósítás a követelmény publikus információs részét szállítja, és előkészíti a későbbi adminisztrációt.

## Kutatási következtetések

A szálláshelyet értékelő látogató a galériát, a részletes szobaadatokat, a szolgáltatásokat, a pontos helyet és az árakat keresi. A boutique szálláshelyeknél a hiányos fürdőszoba- vagy szobainformáció közvetlen bizonytalanságot okoz. Mobilon a kis képernyő miatt az információkat rövid, szemantikailag összetartozó csoportokba kell rendezni.

Ebből következik:

- a gyors tények a részletes narratíva elé kerülnek;
- a szolgáltatások nem egyetlen hosszú listaként jelennek meg;
- a cím és a kapcsolati műveletek külön, könnyen elérhető blokkot kapnak;
- az árak mellett mindig látható az összeg, a pénznem és az egység;
- az adó és a kedvezmények nem rejtett lábjegyzetek;
- az információs tagek vizuálisan nem tűnnek kattintható szűrőknek.

## Tartalomforrás és futásidejű adatfolyam

A YAML szerkesztési forrás, nem futásidejű frontendfüggőség. Az alkalmazás nem tölti be és nem értelmezi közvetlenül a repositoryban lévő YAML-fájlt.

Az adatfolyam:

1. a jóváhagyott YAML tartalma validálható szerkesztési forrás;
2. új, additív Flyway-migráció normalizált adatbázisrekordokat és háromnyelvű seedet hoz létre;
3. a backend modulonkénti publikus query-szerződésekkel összeállítja a részletnézetet;
4. az OpenAPI-szerződés rögzíti a kibővített választ;
5. a frontend kizárólag a publikus API-t fogyasztja;
6. a későbbi adminisztráció ugyanazokat az adatbázisrekordokat módosítja majd, ezért a publikus oldalhoz nem kell második adatút.

Az eredeti `V1__create_guesthouses.sql` nem módosul. A változtatás új `V2` migrációban készül, így meglévő adatbázison is reprodukálható.

## Árjóváhagyás rögzítése

A felhasználó 2026-08-05-én megerősítette, hogy a YAML-ban szereplő árak helyesek és publikálhatók. Az implementáció ennek megfelelően:

- új közvetlen megerősítési forrást ad a YAML `sources` listájához;
- a `shared.pricing.verification.status` értékét `verified` értékre állítja;
- a `publication_requires_owner_confirmation` értékét `false` értékre állítja;
- a `current_prices` blokkoló kérdést eltávolítja az `owner_confirmation_required` listából;
- változatlanul megőrzi az összegeket, pénznemet, egységeket, kedvezményeket, az 1%-os idegenforgalmi adót és a fizetési megjegyzést.

## Adatmodell és modulhatárok

### `accommodation.guesthouse`

A panziómodul tulajdonában marad:

- a név, rövid és részletes bemutatás;
- a közösségi örökség címe és szövege;
- a kapcsolattartó személyek és publikus kapcsolati csatornák;
- a postai cím, lokalizált formázott cím és GPS-koordináták;
- a galéria és annak fordításai.

A meglévő `guesthouse_translation` additív történeti mezőkkel bővül. A kapcsolat és a hely külön, panzióhoz kötött táblákba kerül, még akkor is, ha a kezdőadat jelenleg azonos mindkét panziónál. Ez biztosítja, hogy később egymástól függetlenül módosíthatók legyenek.

A `guesthouse_image.alt_text` átmeneti magyar fallbackként megmaradhat, de a háromnyelvű alternatív szövegek külön `guesthouse_image_translation` rekordokba kerülnek.

### `accommodation.roomtype`

A szobatípusmodul saját entitásokat és fordításokat kap:

- stabil típusazonosító;
- panzióazonosító;
- lokalizált név;
- darabszám;
- standard férőhely;
- pótágyazható szobák száma;
- pótágyak száma jogosult szobánként;
- aktív állapot és megjelenítési sorrend.

Nem készül fizikai `Room` vagy szobaszámmodell. Ez összhangban marad az ADR-004 döntéssel.

### `accommodation.amenity`

A szolgáltatások külön definícióból, fordításból és panzió-hozzárendelésből állnak. A hozzárendelés tartalmazza az aktív állapotot és a megjelenítési sorrendet, így ugyanaz a szolgáltatás később eltérően aktiválható a két épületnél.

Minden publikált szolgáltatás stabil kategóriát kap:

- `ROOM_COMFORT` – szobák és kényelem;
- `FOOD_KITCHEN` – konyha és étkezés;
- `OUTDOOR_WELLNESS` – udvar és kikapcsolódás;
- `PROGRAM_GROUP` – programok és csoportok.

A `needs_owner_confirmation` állapotú kerékpárkölcsönzés és állatmegtekintés inaktív seedként vagy seed nélkül marad; a publikus API nem adja vissza.

### `accommodation.pricing`

Az ármodul tárolja:

- a panzióhoz tartozó aktív árelemeket;
- a lokalizált megnevezést;
- numerikus összeget;
- `RON` pénznemet;
- stabil egységet, például `PERSON_NIGHT`, `PERSON` vagy `DAY`;
- az 1%-os idegenforgalmi adót;
- a 10%-os buszos csoportkedvezményt;
- a 10 év alatti gyermekek 25%-os kedvezményét;
- a háromnyelvű fizetési megjegyzést;
- megjelenítési sorrendet és aktív állapotot.

Ebben a szeletben nincs kalkuláció és nincs végösszeg. A publikus oldal csak a jóváhagyott árlistát jeleníti meg.

### Modulközi összeállítás

A `guesthouse` modul nem olvassa közvetlenül más modul repositoryját vagy JPA-entitását. A részletnézet összeállító szolgáltatása stabil, belső publikus query-interfészeken keresztül kérdezi le a szobatípusokat, szolgáltatásokat és árakat.

## API-szerződés

A `GET /api/guesthouses/{slug}?lang={hu|ro|en}` válasza additívan bővül. A meglévő összefoglaló mezők és a galéria megmaradnak.

Új részletek:

- `history`: lokalizált cím és szöveg;
- `contacts`: típus, érték, publikus címke, preferált jelző és sorrend;
- `address`: lokalizált formázott cím, GPS-szélesség és -hosszúság;
- `roomTypes`: azonosító, név, darabszám, férőhely és pótágyadatok;
- `amenities`: azonosító, lokalizált név/leírás, kategória és sorrend;
- `pricing`: pénznem, árelemek, adó, kedvezmények és lokalizált fizetési megjegyzés;
- `images[].altText`: a kért nyelvhez tartozó alternatív szöveg, magyar fallbackkel.

A szolgáltatásokat a backend lapos, kategóriával ellátott listaként adja vissza. A frontend csoportosítja őket, így az API nem kötődik egy konkrét vizuális elrendezéshez.

A `roomTypes`, `amenities`, `contacts` és `pricing.items` üres lista lehet, ha az adott tartalom nincs publikálható állapotban. A frontend ilyenkor elrejti a teljes üres szekciót. A panzió alapadatai vagy főképe hiányában a jelenlegi detail error állapot marad.

## Frontend információs architektúra

A részletoldal sorrendje:

1. fő kép, panziónév és rövid bemutatás;
2. gyors tények: szobaszám, elhelyezkedés, saját fürdőszoba;
3. öt–hat kiemelt szolgáltatás-tag;
4. képgaléria;
5. részletes bemutatás és közösségi örökség;
6. szobatípus-kártyák;
7. kategorizált szolgáltatások;
8. nyilvános árlista;
9. cím és kapcsolattartási blokk.

### Gyors tények és tagek

A gyors tények rövid, erős tipográfiai egységek. A kiemelt tagek kizárólag a legfontosabb, mindkét panziónál biztosan elérhető szolgáltatásokat mutatják, például Wi-Fi, zárt parkoló, légkondicionálás, közös konyha és wellnessdézsa.

A tagek React-Bootstrap `Badge` vagy szemantikailag egyszerű `span` elemek. Nem gombok, nem szűrők, nincs hover- vagy pointerállapotuk. Az alakjuk visszafogott, enyhén szögletes, és a meglévő arculati színeket használja.

### Szobatípusok

Minden szobatípus külön kártyát kap:

- név;
- rendelkezésre álló darabszám;
- standard férőhely;
- pótágyazhatóság, ha releváns;
- a szobákhoz tartozó alapfelszereltség.

Telefonon egy oszlop, nagyobb képernyőn három oszlop jelenik meg. A kártya nem kattintható, mert ebben a szeletben nincs foglalás vagy külön szobatípus-oldal.

### Szolgáltatások

A teljes publikálható szolgáltatáskészlet négy címezett csoportban jelenik meg. A csoportokon belül a szolgáltatások tömör tagek; hosszabb leírás csak ott jelenik meg, ahol a YAML valódi kiegészítő információt tartalmaz.

Ez elkerüli a 25 elemű, strukturálatlan tagfelhőt, miközben a látogató gyorsan meg tudja találni a számára fontos kényelmi vagy programlehetőséget.

### Árlista

Asztali nézetben az árelemek könnyen összehasonlítható táblázatban, mobilon egymás alá rendezett ár-sorokban jelennek meg. Minden sor tartalmazza:

- a szolgáltatás nevét;
- a numerikus összeget;
- a `RON` pénznemet;
- az egység lokalizált megnevezését.

Az 1%-os idegenforgalmi adó külön figyelmeztető sor. A kedvezmények és a napi árfolyamos HUF/EUR fizetési megjegyzés külön információs blokkba kerülnek. Foglalásra vagy automatikus kalkulációra utaló vezérlő nincs.

### Kapcsolat és helyszín

A blokk tartalmazza a kapcsolattartókat, a teljes lokalizált címet, a preferált telefonszámot, a további számokat és az e-mail-címet. A telefon és az e-mail valódi `tel:` és `mailto:` hivatkozás. A GPS-koordinátákból külső térképlink képezhető, beágyazott térkép nélkül.

Minden interaktív kapcsolati cél legalább 44×44 CSS pixel érintési területet kap. Az információ olvasása nem igényel accordion nyitását.

## Reszponzív és hozzáférhető viselkedés

- A tartalom 320 pixeles szélességtől vízszintes görgetés nélkül használható.
- A szobakártyák és az árlista egy oszlopra törnek.
- A hosszú címkék nem lógnak ki a tagekből.
- A tagek természetes sorokba törnek, és nem kapnak interaktív szerepet.
- A címsorok logikus `h1`–`h2`–`h3` sorrendet követnek.
- A telefonszámok és e-mail-címek látható szövege megegyezik a hivatkozás céljával.
- A háromnyelvű alternatív képszöveg a választott nyelvet követi.
- A jelenlegi galéria Modal billentyűzet- és fókuszkezelése megmarad.
- A reduced-motion beállítás továbbra is érvényes.

## Hibakezelés és tartalmi biztonság

- Hiányzó fordítás esetén magyar fallback használható.
- Nem publikálható vagy inaktív szolgáltatás nem kerül a publikus válaszba.
- Inaktív szobatípus vagy árelem nem jelenik meg.
- Üres opcionális adatszakasz nem eredményez üres dobozt vagy hibás címsort.
- A YAML `editorial_note` mezői nem kerülnek vendégoldalra.
- A közösségi örökség nem jelenik meg a Nisztor család személyes történeteként.
- Az API nem ad vissza belső forrásazonosítót, ellenőrzési megjegyzést vagy tulajdonosi kérdéslistát.
- Kapcsolati adat csak a YAML-ban jóváhagyott publikus adat lehet.

## Tesztelési stratégia

### Tartalomvalidáció

- a YAML szintaktikailag érvényes;
- minden publikus szöveg rendelkezik `hu`, `ro` és `en` változattal;
- a megerősített árak státusza és összege konzisztens;
- a szolgáltatáskategóriák csak az engedélyezett értékeket használják;
- minden galériakép létezik;
- nem megerősített szolgáltatás nem lesz publikálható seed.

### Backend

- Flyway-migráció tiszta PostgreSQL-adatbázison lefut;
- mindkét panzió részletválasza tartalmaz történetet, kapcsolatot, címet, három szobatípust, szolgáltatásokat és árakat;
- HU, RO és EN kérés a megfelelő fordítást adja;
- hiányzó részfordítás magyarra esik vissza;
- inaktív szolgáltatás, szobatípus és árelem nem jelenik meg;
- a kerékpárkölcsönzés és állatmegtekintés nincs a publikus válaszban;
- a response megfelel az OpenAPI-sémának;
- a modularchitektúra-tesztek megakadályozzák a közvetlen repository-/entitásfüggést.

### Frontend

- a részletoldal megjeleníti az összes új szekciót;
- a szolgáltatások a megfelelő csoportba kerülnek;
- a kiemelt tagek nem interaktív elemek;
- a szobatípusok darabszáma és férőhelye helyes;
- az árösszeg, pénznem, egység, adó és kedvezmények olvashatók;
- a telefon- és e-mail-linkek helyes célra mutatnak;
- üres opcionális lista esetén a szekció nem jelenik meg;
- a jelenlegi routing, nyelvváltás és galéria-interakció nem regresszál.

### Vizuális ellenőrzés

A `/hu`, `/ro` és `/en` részletoldalak 320, 768 és 1280 pixeles szélességen ellenőrzendők. Kiemelt ellenőrzési pont a tagtörés, a szobakártyák ritmusa, az árlista mobilos olvashatósága, a hosszú román szövegek és a kapcsolati érintési célok.

## Dokumentáció és nyomonkövetés

Új `docs/features/FR-GH-002-public-detail-content.md` dokumentum készül az elfogadási feltételek részleges leképezésével. A traceability mátrix az információs szeletet megvalósítottként, a teljes `FR-GH-002` követelményt pedig folyamatban lévőként jelöli, mert a foglalás és az adminfelület nincs ebben a hatókörben.

Az OpenAPI-leírás minden backendimplementáció előtt frissül.

## Elkészülési feltételek

A szelet akkor kész, ha:

- a YAML árjóváhagyása rögzítve és validálva van;
- a normalizált V2 migráció minden új tartalmat seedel;
- az API a három nyelven visszaadja a publikálható részletadatokat;
- mindkét panzió részletoldalán megjelenik a jóváhagyott információ;
- a szolgáltatások kategorizált, jól olvasható tagek;
- az árlista egységei, adója és kedvezményei egyértelműek;
- nincs foglalási CTA vagy vendégadat-kezelés;
- a teljes frontend- és backendtesztcsomag átmegy;
- a mobilos és asztali vizuális ellenőrzés nem talál vízszintes görgetést, olvashatatlan szöveget vagy félrevezető interakciót;
- a dokumentáció egyértelműen jelzi az `FR-GH-002` fennmaradó foglalási és adminisztrációs részét.
