# Rendszerarchitektúra

## Cél és hatókör

A Nisztor-Bukovina Platform két bukovinai panzió publikus bemutatását, foglalási kérelmeit és a környék önálló csillagtúra-katalógusát szolgálja ki. A foundation csak a technikai alapot hozza létre; üzleti végpontot, üzleti adatbázistáblát vagy felhasználói folyamatot nem valósít meg.

## Üzleti területek

### Szálláshely (`accommodation`)

A szálláshelyi terület felel a panziókért, a `RoomType` alapú kínálatért, az árképzésért, a szolgáltatásokért és a foglalási kérelmekért. A foglalás nem valós idejű szobafoglalás: a látogató kérelmet küld, a konkrét kapacitást és szobakiosztást a panzió később ellenőrzi.

### Turizmus (`tourism`)

A turisztikai terület a panzióktól független csillagtúrákat, azok sorrendezett programpontjait és tevékenységeit kezeli. A túrakatalógus használata nem része és nem előfeltétele a foglalási folyamatnak.

## Támogató képességek

- `support.authentication`: az adminisztrátori hitelesítés későbbi megvalósításának helye;
- `support.administration`: a két üzleti terület adminisztrációs műveleteinek koordinációja;
- `support.translation`: a többnyelvű tartalom technikai támogatása;
- `support.notification`: a P1 e-mail-folyamatok és a P2 üzenetközvetítő integráció felelőse;
- `shared.configuration`, `shared.exception`, `shared.validation`: üzleti területtől független technikai elemek.

## Rendszerhatárok

A rendszer része a React frontend, a Spring Boot moduláris monolit, a PostgreSQL-adatbázis és a közöttük fennálló relatív `/api` REST-szerződés. A vendégoldal bejelentkezés nélkül használható. A túrakedvencek az MVP-ben kizárólag a böngésző `localStorage` tárhelyén maradnak, nem kerülnek a backendbe és nem szinkronizálódnak eszközök között.

A foundation és az MVP határán kívül, P2-re halasztott képesség az online fizetés, a felhasználói fiók, a konkrét fizikai szobák kezelése, az automatikus szobakiosztás és az AI-alapú útiterv-generálás. Ezek későbbi bővítési lehetőségek, nem a végleges rendszerből tartósan kizárt képességek. A valós idejű szobakészlet kezelése szintén nem része az MVP-nek. A foundation hatókörén kívül van minden üzleti funkció, továbbá a Redis-, RabbitMQ- és e-mail-integráció.

## Függőségi elvek

- Az `accommodation` és a `tourism` között nincs közvetlen függés.
- A támogató képességek nem olvaszthatják össze a két üzleti terület modelljét.
- A `shared` nem függhet üzleti modultól.
- Egy modul belső DAO-ja és JPA-entitása nem lehet másik modul közvetlen szerződése.
- Körkörös csomagfüggőség nem megengedett.
- A frontend és a backend integrációs határa az `openapi.yaml`; a szerződés módosítása megelőzi a két oldal implementációját.
