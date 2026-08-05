# Adatmodell

## Relációs alapelvek

A tartós üzleti adatok relációs PostgreSQL-adatbázisban maradnak. A sémaváltozásokat Flyway verziózza. A modulok a saját üzleti adataikért felelnek, és más modul JPA-entitását nem használják közvetlen szerződésként.

A pénzügyi, férőhely-, dátum- és állapotadatokat a backend a normatív követelmények szerint ismételten validálja. A vendég személyes adatai védendők; teljes foglalási kérés és ellenőrző token nem kerülhet alkalmazáslogba.

## Entitásonkénti fordítási táblák

A magyar, román és angol nyelven szerkeszthető üzleti tartalom entitásonként külön fordítási táblát kap. Nem készül egyetlen általános, minden entitást és mezőt összemosó fordítási tábla. Az alapentitás a nyelvfüggetlen adatot, a hozzá tartozó fordítási tábla az adott entitás fordítható szövegeit tartalmazza. Ez a felosztás az `ADR-003` döntése.

## `RoomType` alapú foglalás

A szállásmodell `RoomType` egységeket kezel, nem konkrét fizikai `Room` rekordokat vagy szobaszámokat. A szobatípus férőhelyet és panzióhoz tartozást fejez ki; a foglalási kérelem szobatípusonkénti darabszámot rögzíthet. A rendszer nem kezel valós idejű szobakészletet, nem választ konkrét szobát, és nem végez automatikus szobakiosztást.

## Megvalósított publikus panzió-adatmodell

Az `FR-GH-001` és az `FR-GH-002` publikus információs szelete az alábbi táblacsoportokat vezeti be:

- a `guesthouse` tartja a nyelvfüggetlen azonosítót, slugot, szobaszámot, sorrendet és az adatok törlése nélkül állítható `active` jelzőt;
- a `guesthouse_translation` panziónként és nyelvenként tárolja a bemutatkozást, a szobaleírást és a történeti tartalmat, magyar fallbackkel;
- a `guesthouse_image` és `guesthouse_image_translation` a panziónként rendezhető képek útvonalát, borítóképjelzőjét és lokalizált alternatív szövegét tartja;
- a `guesthouse_contact` és fordítási táblája a rendezett, aktiválható személy-, telefon- és e-mail-elérhetőségeket tárolja;
- a `guesthouse_address` és fordítási táblája a koordinátát és a lokalizált formázott címet tárolja;
- a `room_type`, `room_type_translation` és `room_type_feature` a panzió szobatípusait, kapacitását, pótágyazását és szolgáltatáskapcsolatait írja le;
- az `amenity`, `amenity_translation` és `guesthouse_amenity` a négy fix kategóriába rendezett, panziónként aktiválható szolgáltatásokat tárolja;
- a `guesthouse_pricing`, `price_item`, `pricing_adjustment` és fordítási tábláik a RON pénznemű ártételeket, felárakat, kedvezményeket és fizetési megjegyzést tárolják.

A két seedelt panzió külön rekord és külön kapcsolt tartalom, ezért egymástól függetlenül módosítható. Az inaktív rekordokat a publikus lekérdezések kizárják, az adatok azonban megmaradnak. A jóváhagyott képfájlok jelenleg a frontend statikus assetjei; az adatbázis csak a relatív útvonalat és a lokalizált metaadatot tárolja. Ez a kis, ritkán változó galériához egyszerű és cache-elhető megoldás, későbbi admin feltöltésnél objektumtárra cserélhető a publikus API módosítása nélkül.

A `roomtype`, `amenity` és `pricing` modul saját `JdbcClient` DAO-t használ, és csak `service` csomagbeli query interfészt, valamint immutable view rekordot ad a részletválasz összeállításához. ArchUnit szabály tiltja, hogy más modul közvetlenül a DAO-kra támaszkodjon.

## Tervezett további táblacsoportok

Az `accommodation` terület következő üzleti táblái a `booking` modul felelősségéhez igazodnak. Ide tartozik a foglalási kérelmek, állapotátmenetek és a követelmények szerinti árazási pillanatkép kezelése; valós idejű szobakészlet továbbra sem része az MVP-nek.

A `tourism` terület későbbi üzleti táblái a `startour` és `activity` modulok felelősségéhez igazodnak. Ide tartozik az önálló csillagtúra, annak sorrendezett programpontjai, tevékenységei és címkéi. Az alternatív programpontok teljes működése `FR-ALT-*` szerint P2.

A `support` terület későbbi perzisztenciája az adminisztráció, hitelesítés, fordítás és értesítés igényeihez igazodhat, de nem olvasztja össze az üzleti modulok tábláit. A böngészőoldali túrakedvencek az MVP-ben nem kerülnek PostgreSQL-be.

## Migrációs állapot

Az első Flyway migráció az `FR-GH-001` alap panziómodelljét és a két kezdeti panziórekordot hozza létre. A második migráció az `FR-GH-002` publikus információs szeletéhez szükséges normalizált, háromnyelvű szoba-, szolgáltatás-, ár-, kapcsolat-, cím-, történet- és képmetaadatokat vezeti be. Foglalási és adminisztrációs adatmodell még nincs.
