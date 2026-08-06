# Nisztor és Bukovina panzió tartalomcsomag — terv

## Cél

Egyetlen, kódon és adatbázison kívül tárolt YAML-fájl készüljön a Nisztor Panzió és a Bukovina Panzió publikus részletes oldalának tartalmáról. A fájl legyen egyszerre ember számára áttekinthető és fejlesztő AI számára egyértelműen feldolgozható. A tartalom magyar, román és angol nyelven készül.

A tervezett fájl:

`docs/content/guesthouses.hu-ro-en.yaml`

## Hatókör

A tartalomcsomag mindkét panzió esetében tartalmazza:

- a panzió nevét és stabil slugját;
- rövid és részletes bemutatását;
- eredet- vagy családtörténetét;
- kapcsolattartási adatait;
- postai címét és GPS-koordinátáit;
- fő képét és rendezett képgalériáját;
- szobatípusait, darabszámát és férőhelyadatait;
- szolgáltatásait;
- nyilvános árlistáját, pénznemét, egységeit és feltételeit;
- a foglalási kérelem indításához szükséges mezőket;
- a felhasznált forrásokat és az adatok ellenőrzési állapotát.

Az alkalmazás, az adatbázis, az API és a frontend módosítása nem része ennek a munkának.

## Tartalmi felosztás

A YAML két szintre választja az adatokat:

1. `shared`: a jelenleg mindkét panzióra azonos kapcsolattartás, cím, GPS-hely, szolgáltatások, árlista és foglalási űrlap;
2. `guesthouses`: a Nisztor és Bukovina panzió saját neve, leírásai, története, szobatípusai és galériája.

Ez a felosztás elkerüli a közös adatok másolását, miközben lehetővé teszi, hogy később bármely közös adat panzióspecifikussá váljon.

## Nyelvi modell

Minden vendég által olvasott szöveg `hu`, `ro` és `en` kulcs alatt szerepel. A magyar szöveg a meglévő magyar oldalak és a jelenlegi projektadatok szerkesztett változata. A román és angol szöveg professzionális, jelentéshű lokalizáció, nem szó szerinti fordítás.

A nyelvfüggetlen értékek — például telefonszám, e-mail, GPS-koordináta, árösszeg, pénznem, képfájlútvonal és darabszám — csak egyszer szerepelnek.

## Forrásbiztonság és bizonytalanság

Minden forráscsoporthoz URL és hozzáférési dátum tartozik. A tartalomcsomag három állapotot használ:

- `verified`: a megadott weboldalon vagy a projekt jóváhagyott kezdőadataiban közvetlenül megtalálható;
- `derived`: hiteles forrásadatból szerkesztett vagy összesített érték;
- `needs_owner_confirmation`: nem bizonyítható egyértelműen, elavulhatott, vagy tulajdonosi jóváhagyást igényel.

A konkrét Nisztor család története nem vezethető le automatikusan a bukovinai székely közösség történetéből. A YAML ezért külön kezeli a `family_story` és a `community_heritage` mezőt. Ha az elsőhöz nincs hiteles forrás, nem készül kitalált narratíva; helyette tulajdonosi megerősítést kérő megjegyzés szerepel.

Az árlista időérzékeny adat. A forrásoldal összegei bekerülnek, de ellenőrzési dátumot és publikálás előtti tulajdonosi megerősítési jelzést kapnak.

## Tervezett YAML-szerkezet

```yaml
schema_version: 1
document:
  title: {}
  purpose: {}
  last_researched_at: 2026-08-05
  default_language: hu
  supported_languages: [hu, ro, en]

sources: []

shared:
  contact: {}
  address: {}
  services: []
  pricing:
    currency: RON
    tax: {}
    items: []
    discounts: []
    payment_notes: {}
  booking_request:
    action: inquiry
    fields: []
    consent: {}

guesthouses:
  - id: nisztor
    slug: nisztor-panzio
    content: {}
    history:
      family_story: {}
      community_heritage: {}
    rooms: []
    media:
      cover: {}
      gallery: []
  - id: bukovina
    slug: bukovina-panzio
    content: {}
    history: {}
    rooms: []
    media: {}

owner_confirmation_required: []
```

Minden fordítható objektum ugyanazt a mintát követi:

```yaml
label:
  hu: Magyar szöveg
  ro: Text în limba română
  en: English text
```

## Képek

A tartalomcsomag a projektben már megtalálható 26 Nisztor- és 34 Bukovina-kép publikus útvonalát használja. Panziónként egy kép `cover: true` jelölést kap, a galéria sorrendje számozott. Az alternatív képszövegek három nyelven készülnek, ahol a kép tartalma a fájlból biztonsággal azonosítható. Általános, félrevezető alternatív szöveg helyett bizonytalan esetben ellenőrzési jelzés kerül az elemhez.

## Foglalási kérelem

A foglalás nem azonnali, garantált szobafoglalás, hanem kapcsolatfelvételi kérelem. A struktúra legalább a következő mezőket írja le:

- név;
- e-mail;
- telefon;
- választott panzió;
- érkezés és távozás dátuma;
- felnőttek és gyermekek száma;
- választott szobatípusok vagy szobaszámigény;
- ellátási igény;
- megjegyzés;
- adatkezelési hozzájárulás.

Minden mezőnél szerepel a stabil mezőazonosító, a háromnyelvű címke, a típus és a kötelezőség. A fájl nem tartalmaz vendégadatot.

## Minőség-ellenőrzés

A kész YAML ellenőrzése négy lépésből áll:

1. YAML-szintaktikai feldolgozhatóság;
2. minden vendégoldali szöveg HU–RO–EN teljességének ellenőrzése;
3. helyi képútvonalak létezésének és a sorszámok folytonosságának ellenőrzése;
4. forrás nélküli tényállítások, hiányzó mértékegységek és nem jelölt bizonytalanságok keresése.

## Elkészülési feltételek

A tartalomcsomag akkor kész, ha:

- egyetlen YAML-fájlban megtalálható mindkét panzió és minden közös adat;
- minden publikus szöveg három nyelven rendelkezésre áll;
- a Nisztor és Bukovina szobakínálata külön kezelhető;
- az árak géppel feldolgozható összegek és egységek;
- a képek a projekt valós fájljaira hivatkoznak;
- a bizonytalan vagy időérzékeny adatok egyértelmű jelölést kapnak;
- a fájl a kód ismerete nélkül is érthető;
- a fájl szintaktikailag érvényes YAML.
