CREATE TABLE guesthouse (
    id UUID PRIMARY KEY,
    slug VARCHAR(80) NOT NULL UNIQUE,
    room_count INTEGER NOT NULL CHECK (room_count > 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0)
);

CREATE TABLE guesthouse_translation (
    guesthouse_id UUID NOT NULL REFERENCES guesthouse (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    name VARCHAR(160) NOT NULL,
    short_description VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    room_description TEXT NOT NULL,
    PRIMARY KEY (guesthouse_id, language_code)
);

CREATE TABLE guesthouse_image (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guesthouse_id UUID NOT NULL REFERENCES guesthouse (id) ON DELETE CASCADE,
    path VARCHAR(500) NOT NULL,
    alt_text VARCHAR(300) NOT NULL,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    cover BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (guesthouse_id, display_order)
);

CREATE UNIQUE INDEX uq_guesthouse_cover_image
    ON guesthouse_image (guesthouse_id)
    WHERE cover;

-- Approved initial content snapshot from https://www.nisztorpanzio.ro/ on 2026-08-04.
INSERT INTO guesthouse (id, slug, room_count, active, display_order)
VALUES
    ('82b508e1-2893-4f45-8cc8-7a6f50b43a4d', 'nisztor-panzio', 5, TRUE, 1),
    ('7aaf8670-c258-41ec-b328-93823fe0472f', 'bukovina-panzio', 12, TRUE, 2);

INSERT INTO guesthouse_translation (
    guesthouse_id,
    language_code,
    name,
    short_description,
    description,
    room_description
)
VALUES
    (
        '82b508e1-2893-4f45-8cc8-7a6f50b43a4d',
        'hu',
        'Nisztor Panzió',
        'Csendes, nyugodt, családias környezet Csernakeresztúr központjától alig 200 méterre.',
        'Panziónk Csernakeresztúr központjától alig 200 méterre található. Csendes, nyugodt, családias környezetben igényes szálláslehetőséget kínálunk minden betérő vendégünknek. Falunk Dél-Erdélyben, Déva és Vajdahunyad között félúton helyezkedik el. Őseink bukovinai székelyek, ezért Csernakeresztúr lakosságának több mint fele magyar anyanyelvű.',
        'A Nisztor Panzió épületében 5 szoba található: 3 kétágyas szoba, amelyek közül az egyik pótágyazható, 1 háromágyas és 1 négyágyas szoba. Mindegyik szobához külön fürdőszoba és televízió tartozik.'
    ),
    (
        '7aaf8670-c258-41ec-b328-93823fe0472f',
        'hu',
        'Bukovina Panzió',
        'Csendes, nyugodt, családias környezetben igényes szálláslehetőséget kínálunk.',
        'Csendes, nyugodt, családias környezetben igényes szálláslehetőséget kínálunk minden betérő vendégünknek. Alkalmat adunk pihenésre vagy aktív kikapcsolódásra, akár átutazóban vannak, akár többnapos szabadságra érkeznek. Vendégeinket tiszta, rendezett szobák és kedves, udvarias kiszolgálás várja.',
        'A Bukovina Panzió épületében 12 szoba található: 6 kétágyas szoba, amelyek közül az egyik pótágyazható, 5 háromágyas és 1 négyágyas szoba. Mindegyik szobához külön fürdőszoba és televízió tartozik.'
    );

INSERT INTO guesthouse_image (guesthouse_id, path, alt_text, display_order, cover)
SELECT
    '82b508e1-2893-4f45-8cc8-7a6f50b43a4d',
    '/images/guesthouses/nisztor/gallery-' || LPAD(image_number::TEXT, 2, '0') || '.jpg',
    'Nisztor Panzió - galériakép ' || image_number,
    image_number - 1,
    image_number = 1
FROM generate_series(1, 26) AS series(image_number);

INSERT INTO guesthouse_image (guesthouse_id, path, alt_text, display_order, cover)
SELECT
    '7aaf8670-c258-41ec-b328-93823fe0472f',
    '/images/guesthouses/bukovina/gallery-' || LPAD(image_number::TEXT, 2, '0') || '.jpg',
    'Bukovina Panzió - galériakép ' || image_number,
    image_number - 1,
    image_number = 1
FROM generate_series(1, 34) AS series(image_number);
