# Foglalási értesítések üzemeltetése

A foglalás létrehozása és az e-mail-küldés külön lépés. A backend ugyanabban az adatbázis-tranzakcióban menti el a foglalást és a kézbesítési feladatokat, majd a beépített háttér-worker küldi el őket. Ehhez nem kell RabbitMQ vagy külön folyamat.

## Környezeti változók

```dotenv
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM_ADDRESS=
MAIL_FROM_NAME=Nisztor-Bukovina Platform
MAIL_SMTP_AUTH=true
MAIL_STARTTLS_ENABLE=true

NOTIFICATIONS_ENABLED=true
BOOKING_NOTIFICATION_MAX_ATTEMPTS=5
BOOKING_NOTIFICATION_RETRY_INITIAL_DELAY_SECONDS=60
BOOKING_NOTIFICATION_WORKER_DELAY=PT10S
BOOKING_NOTIFICATION_TOKEN_ENCRYPTION_KEY=
APP_PUBLIC_BASE_URL=https://example.com
APP_ADMIN_BASE_URL=https://example.com
```

A `BOOKING_NOTIFICATION_TOKEN_ENCRYPTION_KEY` egy véletlen, pontosan 32 bájtos AES-kulcs Base64 alakban. Helyben például az `openssl rand -base64 32` paranccsal készíthető. A kulcsot és az SMTP-jelszót csak a futtatási környezet titokkezelőjében vagy a gitből kizárt helyi `.env` fájlban szabad tárolni.

Gmail használatakor a `MAIL_PASSWORD` alkalmazásjelszó legyen, ne a postafiók normál jelszava. Éles domainnél az SPF, DKIM és DMARC rekordokat is be kell állítani, különben az üzenetek könnyen spambe kerülnek vagy elutasításra kerülnek.

## Panziónkénti belső címzettek

A belső címzettek adatbázisban vannak, és nem azonosak a publikus kapcsolattartási e-mail-címmel. Ugyanaz az e-mail-cím több panzióhoz is hozzárendelhető. Egy módosítás csak az ezután létrejövő kézbesítési feladatokra hat; a már sorba tett üzenetek címzettje nem változik.

```sql
SELECT id, slug FROM guesthouse ORDER BY slug;

INSERT INTO guesthouse_notification_recipient (id, guesthouse_id, email)
VALUES (
    gen_random_uuid(),
    (SELECT id FROM guesthouse WHERE slug = 'bukovina-panzio'),
    'admin@example.com'
);
```

Címzett kikapcsolása:

```sql
UPDATE guesthouse_notification_recipient
SET active = FALSE, updated_at = CURRENT_TIMESTAMP
WHERE guesthouse_id = (SELECT id FROM guesthouse WHERE slug = 'bukovina-panzio')
  AND LOWER(email) = LOWER('admin@example.com');
```

## Biztonsági működés

- A vendég a nyers management tokent csak az e-mailes HTTPS-linkben kapja meg.
- A foglalás rekordjában csak a token SHA-256 hash-e marad.
- A kézbesítésig az outbox AES-GCM titkosítva tartja a tokent; sikeres kézbesítés vagy a próbálkozások kimerülése után törli a titkosított értéket is.
- A worker csak biztonságos hibakódot tárol, címet, tokent és teljes üzenettörzset nem naplóz.
- A vendégnek küldött `GET` link önmagában nem módosíthat foglalási állapotot.
- A levél feladója a rendszer `MAIL_FROM_ADDRESS` címe lehet, de a `Reply-To` fejléc mindig az érintett panzió aktív publikus e-mail-címe. A levelezőprogram Válasz funkciója ezért például a `nisztorpanzio@gmail.com` címre, nem a rendszerfeladónak küldi az üzenetet; ezt a HTML- és szöveges levél is egyértelműen jelzi.
