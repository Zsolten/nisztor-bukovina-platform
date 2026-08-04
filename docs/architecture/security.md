# Biztonsági architektúra

## Felhasználói határ

A publikus látogató anonim: panziókat, árakat, szolgáltatásokat és csillagtúrákat bejelentkezés nélkül tekinthet meg, foglalási kérelmet küldhet, és böngészőoldali kedvenceket kezelhet. Az MVP nem tartalmaz látogatói regisztrációt vagy felhasználói fiókot.

Az adminisztráció kizárólag hitelesített adminisztrátor számára érhető el az `FR-ADMIN-001` szerint. A foundationben az adminhitelesítés konkrét token- vagy sessionmechanizmusa nincs kiválasztva és nincs megvalósítva; ezt későbbi security ADR és feature PR rögzíti. Ez a technikai halasztás nem módosítja az adminhitelesítés P0 követelményét.

## Ideiglenes foundation konfiguráció

A Spring Security függőség és konfiguráció jelen van, de a foundation minden alkalmazásútvonalat explicit módon engedélyez. Ez a `permit-all` állapot átmeneti technikai alap, nem az adminfelület végleges biztonsági modellje. Üzleti adminvégpont csak a hozzá tartozó hitelesítéssel és jogosulatlan hozzáférést bizonyító teszttel együtt vezethető be.

## Validáció

Minden felhasználói bemenetet frontend- és backendoldalon is validálni kell, de a backend ellenőrzése az irányadó. Kötelező a mezőhossz, e-mail, telefonszám, dátumtartomány, nem negatív létszám és szobaszám, valamint a hivatkozott panzió és szobatípus ellenőrzése. A szabad szöveg nem renderelhető HTML-ként. Az árakat, adókat, férőhelyet és végösszeget a backend a kliens értékétől függetlenül újraszámítja.

## Adatvédelem

A név, e-mail-cím, telefonszám, utazási dátum és foglalási megjegyzés személyes adat. A teljes foglalási kérés nem kerül alkalmazáslogba, az e-mail-cím csak maszkolva jelenhet meg, az e-mail-ellenőrző token pedig nem naplózható. A `localStorage` nem tartalmazhat foglalási, személyes vagy más érzékeny adatot; a kedvencek kizárólag túraazonosítókat tartalmazhatnak, metaadatot nem.

Az `FR-FAV-002` ajánlott tárolási struktúrájában megjelenő metaadat és az `NFR-PRIV-001` P0 elfogadási feltétele között látszólagos feszültség van. A normatív specifikáció tisztázásáig a szigorúbb P0 adatvédelmi feltétel az irányadó: a kedvencek tárolása csak azonosítókat használhat.

## Visszaélésvédelem

A publikus foglalási endpoint rate limitje `NFR-SEC-001` szerint P0: IP-címenként 5 kérés 10 perc alatt, e-mail-címenként 3 kérés óránként, valamint IP-címenként 20 kérés naponta. Limit elérésekor HTTP 429 válasz szükséges; a `Retry-After` fejléc megengedett, de nem kötelező. A korlátozás szerveroldali, és a Redis kizárólag ennek technikai támogatására tervezett.

Az admin login IP- és admin e-mail-cím szerinti korlátozása `NFR-SEC-004` alapján P0. Nem létező e-mail és hibás jelszó azonos hibaüzenetet ad, a jelszó Argon2id vagy bcrypt használatával tárolandó. A botvédelem `NFR-SEC-002` alapján P1, szerveroldali tokenellenőrzéssel.
