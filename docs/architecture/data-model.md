# Adatmodell

## Relációs alapelvek

A tartós üzleti adatok relációs PostgreSQL-adatbázisban maradnak. A sémaváltozásokat Flyway verziózza. A modulok a saját üzleti adataikért felelnek, és más modul JPA-entitását nem használják közvetlen szerződésként.

A pénzügyi, férőhely-, dátum- és állapotadatokat a backend a normatív követelmények szerint ismételten validálja. A vendég személyes adatai védendők; teljes foglalási kérés és ellenőrző token nem kerülhet alkalmazáslogba.

## Entitásonkénti fordítási táblák

A magyar, román és angol nyelven szerkeszthető üzleti tartalom entitásonként külön fordítási táblát kap. Nem készül egyetlen általános, minden entitást és mezőt összemosó fordítási tábla. Az alapentitás a nyelvfüggetlen adatot, a hozzá tartozó fordítási tábla az adott entitás fordítható szövegeit tartalmazza. Ez a felosztás az `ADR-003` döntése.

## `RoomType` alapú foglalás

A szállásmodell `RoomType` egységeket kezel, nem konkrét fizikai `Room` rekordokat vagy szobaszámokat. A szobatípus férőhelyet és panzióhoz tartozást fejez ki; a foglalási kérelem szobatípusonkénti darabszámot rögzíthet. A rendszer nem kezel valós idejű szobakészletet, nem választ konkrét szobát, és nem végez automatikus szobakiosztást.

## Tervezett táblacsoportok

Az `accommodation` terület későbbi üzleti táblái a `guesthouse`, `roomtype`, `pricing`, `amenity` és `booking` modulok felelősségéhez igazodnak. Ide tartozik a két panzió tartalma, a szobatípusok, az érvényes árpolitika, a panziónkénti szolgáltatások és a foglalási kérelmek állapota.

A `tourism` terület későbbi üzleti táblái a `startour` és `activity` modulok felelősségéhez igazodnak. Ide tartozik az önálló csillagtúra, annak sorrendezett programpontjai, tevékenységei és címkéi. Az alternatív programpontok teljes működése `FR-ALT-*` szerint P2.

A `support` terület későbbi perzisztenciája az adminisztráció, hitelesítés, fordítás és értesítés igényeihez igazodhat, de nem olvasztja össze az üzleti modulok tábláit. A böngészőoldali túrakedvencek az MVP-ben nem kerülnek PostgreSQL-be.

## Foundation állapot

A foundation nem hoz létre üzleti táblát, seed adatot vagy üres, mesterséges üzleti migrációt. Az első Flyway migráció az első valódi adatmodellt megvalósító feature-rel együtt készül.
