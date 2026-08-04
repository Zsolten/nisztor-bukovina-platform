# Adatmodell

## Relációs alapelvek

A tartós üzleti adatok relációs PostgreSQL-adatbázisban maradnak. A sémaváltozásokat Flyway verziózza. A modulok a saját üzleti adataikért felelnek, és más modul JPA-entitását nem használják közvetlen szerződésként.

A pénzügyi, férőhely-, dátum- és állapotadatokat a backend a normatív követelmények szerint ismételten validálja. A vendég személyes adatai védendők; teljes foglalási kérés és ellenőrző token nem kerülhet alkalmazáslogba.

## Entitásonkénti fordítási táblák

A magyar, román és angol nyelven szerkeszthető üzleti tartalom entitásonként külön fordítási táblát kap. Nem készül egyetlen általános, minden entitást és mezőt összemosó fordítási tábla. Az alapentitás a nyelvfüggetlen adatot, a hozzá tartozó fordítási tábla az adott entitás fordítható szövegeit tartalmazza. Ez a felosztás az `ADR-003` döntése.

## `RoomType` alapú foglalás

A szállásmodell `RoomType` egységeket kezel, nem konkrét fizikai `Room` rekordokat vagy szobaszámokat. A szobatípus férőhelyet és panzióhoz tartozást fejez ki; a foglalási kérelem szobatípusonkénti darabszámot rögzíthet. A rendszer nem kezel valós idejű szobakészletet, nem választ konkrét szobát, és nem végez automatikus szobakiosztást.

## Megvalósított `guesthouse` táblacsoport

Az `FR-GH-001` három táblát vezet be:

- a `guesthouse` tartja a nyelvfüggetlen azonosítót, slugot, szobaszámot, sorrendet és az adatok törlése nélkül állítható `active` jelzőt;
- a `guesthouse_translation` panziónként és nyelvenként tárolja a publikus szövegeket, magyar fallbackkel;
- a `guesthouse_image` a panziónként külön rendezhető képek útvonalát, alternatív szövegét és borítóképjelzőjét tartja.

A két seedelt panzió külön rekord és külön kapcsolt tartalom, ezért egymástól függetlenül módosítható. Az inaktív rekordokat a publikus repository-lekérdezések kizárják, az adatok azonban megmaradnak. A jóváhagyott képfájlok jelenleg a frontend statikus assetjei; az adatbázis csak a relatív útvonalat és a metaadatot tárolja. Ez a kis, ritkán változó galériához egyszerű és cache-elhető megoldás, későbbi admin feltöltésnél objektumtárra cserélhető a publikus API módosítása nélkül.

## Tervezett további táblacsoportok

Az `accommodation` terület további üzleti táblái a `roomtype`, `pricing`, `amenity` és `booking` modulok felelősségéhez igazodnak. Ide tartozik a részletes szobatípusmodell, az érvényes árpolitika, a panziónkénti szolgáltatások és a foglalási kérelmek állapota.

A `tourism` terület későbbi üzleti táblái a `startour` és `activity` modulok felelősségéhez igazodnak. Ide tartozik az önálló csillagtúra, annak sorrendezett programpontjai, tevékenységei és címkéi. Az alternatív programpontok teljes működése `FR-ALT-*` szerint P2.

A `support` terület későbbi perzisztenciája az adminisztráció, hitelesítés, fordítás és értesítés igényeihez igazodhat, de nem olvasztja össze az üzleti modulok tábláit. A böngészőoldali túrakedvencek az MVP-ben nem kerülnek PostgreSQL-be.

## Migrációs állapot

Az első Flyway migráció az `FR-GH-001` valódi adatmodelljét és a két jóváhagyott kezdeti panziórekordot hozza létre. Nem tartalmaz foglalási, árazási, szolgáltatási vagy adminisztrációs adatmodellt.
