ALTER TABLE guesthouse_translation
    ADD COLUMN history_title VARCHAR(240),
    ADD COLUMN history_text TEXT;

CREATE TABLE guesthouse_image_translation (
    image_id UUID NOT NULL REFERENCES guesthouse_image (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    alt_text VARCHAR(300) NOT NULL,
    PRIMARY KEY (image_id, language_code)
);

CREATE TABLE guesthouse_contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guesthouse_id UUID NOT NULL REFERENCES guesthouse (id) ON DELETE CASCADE,
    code VARCHAR(80) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('PERSON', 'PHONE', 'EMAIL')),
    value VARCHAR(320) NOT NULL,
    preferred BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    UNIQUE (guesthouse_id, code)
);

CREATE TABLE guesthouse_contact_translation (
    contact_id UUID NOT NULL REFERENCES guesthouse_contact (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    label VARCHAR(160) NOT NULL,
    PRIMARY KEY (contact_id, language_code)
);

CREATE TABLE guesthouse_address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guesthouse_id UUID NOT NULL UNIQUE REFERENCES guesthouse (id) ON DELETE CASCADE,
    latitude NUMERIC(8, 5) NOT NULL,
    longitude NUMERIC(8, 5) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE guesthouse_address_translation (
    address_id UUID NOT NULL REFERENCES guesthouse_address (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    formatted_address VARCHAR(500) NOT NULL,
    PRIMARY KEY (address_id, language_code)
);

CREATE TABLE amenity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(80) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL CHECK (
        category IN ('ROOM_COMFORT', 'FOOD_KITCHEN', 'OUTDOOR_WELLNESS', 'PROGRAM_GROUP')
    )
);

CREATE TABLE amenity_translation (
    amenity_id UUID NOT NULL REFERENCES amenity (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    name VARCHAR(240) NOT NULL,
    description TEXT,
    PRIMARY KEY (amenity_id, language_code)
);

CREATE TABLE guesthouse_amenity (
    guesthouse_id UUID NOT NULL REFERENCES guesthouse (id) ON DELETE CASCADE,
    amenity_id UUID NOT NULL REFERENCES amenity (id) ON DELETE CASCADE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    PRIMARY KEY (guesthouse_id, amenity_id)
);

CREATE TABLE room_type (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guesthouse_id UUID NOT NULL REFERENCES guesthouse (id) ON DELETE CASCADE,
    code VARCHAR(80) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    standard_occupancy INTEGER NOT NULL CHECK (standard_occupancy > 0),
    rooms_with_extra_bed INTEGER NOT NULL CHECK (rooms_with_extra_bed >= 0),
    extra_beds_per_eligible_room INTEGER NOT NULL CHECK (extra_beds_per_eligible_room >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    UNIQUE (guesthouse_id, code)
);

CREATE TABLE room_type_translation (
    room_type_id UUID NOT NULL REFERENCES room_type (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    name VARCHAR(160) NOT NULL,
    PRIMARY KEY (room_type_id, language_code)
);

CREATE TABLE room_type_feature (
    room_type_id UUID NOT NULL REFERENCES room_type (id) ON DELETE CASCADE,
    amenity_id UUID NOT NULL REFERENCES amenity (id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    PRIMARY KEY (room_type_id, amenity_id)
);

CREATE TABLE guesthouse_pricing (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    guesthouse_id UUID NOT NULL UNIQUE REFERENCES guesthouse (id) ON DELETE CASCADE,
    currency VARCHAR(3) NOT NULL CHECK (currency = 'RON'),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE guesthouse_pricing_translation (
    pricing_id UUID NOT NULL REFERENCES guesthouse_pricing (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    payment_note TEXT NOT NULL,
    PRIMARY KEY (pricing_id, language_code)
);

CREATE TABLE price_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pricing_id UUID NOT NULL REFERENCES guesthouse_pricing (id) ON DELETE CASCADE,
    code VARCHAR(80) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    unit VARCHAR(20) NOT NULL CHECK (unit IN ('person_night', 'person', 'day')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    UNIQUE (pricing_id, code)
);

CREATE TABLE price_item_translation (
    price_item_id UUID NOT NULL REFERENCES price_item (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    label VARCHAR(240) NOT NULL,
    PRIMARY KEY (price_item_id, language_code)
);

CREATE TABLE pricing_adjustment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pricing_id UUID NOT NULL REFERENCES guesthouse_pricing (id) ON DELETE CASCADE,
    code VARCHAR(80) NOT NULL,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('SURCHARGE', 'DISCOUNT')),
    percentage NUMERIC(5, 2) NOT NULL CHECK (percentage >= 0 AND percentage <= 100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    UNIQUE (pricing_id, code)
);

CREATE TABLE pricing_adjustment_translation (
    adjustment_id UUID NOT NULL REFERENCES pricing_adjustment (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    label VARCHAR(240) NOT NULL,
    PRIMARY KEY (adjustment_id, language_code)
);

INSERT INTO guesthouse_translation (
    guesthouse_id, language_code, name, short_description, description,
    room_description, history_title, history_text
)
SELECT id, 'hu', 'Nisztor Panzió',
       'Csendes, családias szállás Csernakeresztúr központjától mindössze 200 méterre.',
       'A Nisztor Panzió Csernakeresztúron, Déva és Vajdahunyad között félúton, a falu központjától mindössze 200 méterre várja vendégeit. A főúttól távol eső, nyugodt környezet ideális egy rövid megállóhoz és többnapos pihenéshez is. Az öt rendezett, kényelmes szobához saját fürdőszoba, televízió, központi fűtés és légkondicionálás tartozik. Minden emeleten felszerelt közös konyha, a kikapcsolódáshoz pedig hidromasszázs- és aeromasszázs-funkciós wellnessdézsa, kültéri zuhanyzó és napozóágyak állnak rendelkezésre. A házigazdák családias vendéglátással, házi jellegű erdélyi magyaros ételekkel, valamint igény szerint program- és csillagtúra-ajánlatokkal teszik teljessé az itt-tartózkodást.',
       'A Nisztor Panzió épületében 5 szoba található: 3 kétágyas szoba, amelyek közül az egyik pótágyazható, 1 háromágyas és 1 négyágyas szoba.',
       'Bukovinai székely örökség Csernakeresztúron',
       'Csernakeresztúr lakosságának több mint fele bukovinai székely gyökerekkel rendelkezik. Őseik az 1764-es madéfalvi veszedelem után Bukovinába menekültek, ahol Istensegíts, Fogadjisten, Hadikfalva, Andrásfalva és Józseffalva közösségeiben telepedtek le. A magyar állam 1911-ben telepített családokat a mai Csernakeresztúrra. Az új közösség 1915–1916-ban templomot épített, 1920-ban pedig katolikus iskolát indított. A bukovinai székely hagyományokat és néptáncokat a helyiek nemzedékről nemzedékre továbbadják.'
FROM guesthouse WHERE slug = 'nisztor-panzio'
ON CONFLICT (guesthouse_id, language_code) DO UPDATE SET
    name = EXCLUDED.name,
    short_description = EXCLUDED.short_description,
    description = EXCLUDED.description,
    room_description = EXCLUDED.room_description,
    history_title = EXCLUDED.history_title,
    history_text = EXCLUDED.history_text;

INSERT INTO guesthouse_translation (
    guesthouse_id, language_code, name, short_description, description,
    room_description, history_title, history_text
)
SELECT id, 'ro', 'Pensiunea Nisztor',
       'Cazare liniștită și primitoare, la doar 200 de metri de centrul satului Cristur.',
       'Pensiunea Nisztor își întâmpină oaspeții în satul Cristur, la jumătatea drumului dintre Deva și Hunedoara și la numai 200 de metri de centrul localității. Amplasarea liniștită, departe de traficul șoselei principale, este potrivită atât pentru un popas scurt, cât și pentru un sejur de mai multe zile. Cele cinci camere îngrijite și confortabile au baie proprie, televizor, încălzire centrală și aer condiționat. La fiecare etaj se află o bucătărie comună utilată, iar pentru relaxare sunt disponibile un ciubăr wellness cu hidromasaj și aeromasaj, duș exterior și șezlonguri. Ospitalitatea caldă, preparatele ardelenești și ungurești cu caracter tradițional, precum și recomandările de programe și excursii completează experiența șederii.',
       'Pensiunea Nisztor are 5 camere: 3 camere duble, dintre care una permite un pat suplimentar, 1 cameră triplă și 1 cameră cvadruplă.',
       'Moștenirea secuilor bucovineni din Cristur',
       'Mai mult de jumătate dintre locuitorii satului Cristur au rădăcini secuiești bucovinene. Strămoșii lor s-au refugiat în Bucovina după evenimentele de la Siculeni din 1764 și s-au așezat în comunitățile Istensegíts, Fogadjisten, Hadikfalva, Andrásfalva și Józseffalva. În 1911, statul maghiar a strămutat familii în actualul Cristur. Noua comunitate a ridicat o biserică în anii 1915–1916 și a deschis o școală catolică în 1920. Tradițiile și dansurile secuilor bucovineni sunt transmise și astăzi din generație în generație.'
FROM guesthouse WHERE slug = 'nisztor-panzio'
ON CONFLICT (guesthouse_id, language_code) DO UPDATE SET
    name = EXCLUDED.name,
    short_description = EXCLUDED.short_description,
    description = EXCLUDED.description,
    room_description = EXCLUDED.room_description,
    history_title = EXCLUDED.history_title,
    history_text = EXCLUDED.history_text;

INSERT INTO guesthouse_translation (
    guesthouse_id, language_code, name, short_description, description,
    room_description, history_title, history_text
)
SELECT id, 'en', 'Nisztor Guesthouse',
       'Peaceful, welcoming accommodation just 200 metres from the centre of Cristur.',
       'Nisztor Guesthouse welcomes visitors in Cristur, halfway between Deva and Hunedoara and only 200 metres from the village centre. Its peaceful setting away from main-road traffic suits both an overnight stop and a longer stay. Each of the five tidy, comfortable rooms has a private bathroom, television, central heating, and air conditioning. Every floor has an equipped shared kitchen, while a wellness tub with hydro- and air massage, an outdoor shower, and sun loungers provide space to unwind. Warm family hospitality, homemade Transylvanian-Hungarian food, and optional programme and day-trip suggestions round out the experience.',
       'Nisztor Guesthouse has 5 rooms: 3 double rooms, one of which can take an extra bed, 1 triple room, and 1 quadruple room.',
       'Bukovina Szekler heritage in Cristur',
       'More than half of Cristur''s residents have Bukovina Szekler roots. Their ancestors fled to Bukovina after the 1764 events at Siculeni and settled in the communities of Istensegíts, Fogadjisten, Hadikfalva, Andrásfalva, and Józseffalva. In 1911, the Hungarian state resettled families in present-day Cristur. The new community built a church in 1915–1916 and opened a Catholic school in 1920. Bukovina Szekler traditions and folk dances continue to be passed down from one generation to the next.'
FROM guesthouse WHERE slug = 'nisztor-panzio'
ON CONFLICT (guesthouse_id, language_code) DO UPDATE SET
    name = EXCLUDED.name,
    short_description = EXCLUDED.short_description,
    description = EXCLUDED.description,
    room_description = EXCLUDED.room_description,
    history_title = EXCLUDED.history_title,
    history_text = EXCLUDED.history_text;

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Ötfős csoport népviseleti ruhákban a Nisztor Panzió előtt'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-01.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Grup de cinci persoane în costume tradiționale în fața Pensiunii Nisztor'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-01.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Group of five people in traditional clothing in front of Nisztor Guesthouse'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-01.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Virágos erkély a Nisztor Panzió homlokzatán'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-02.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Balcon cu flori pe fațada Pensiunii Nisztor'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-02.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Flower-filled balcony on the Nisztor Guesthouse façade'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-02.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Két személy házi befőttekkel és savanyúságokkal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-03.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Două persoane alături de conserve și murături pregătite în casă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-03.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Two people with homemade preserves and pickles'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-03.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Házi savanyúságokkal megrakott tál'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-04.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Platou cu murături pregătite în casă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-04.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Display of homemade pickles'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-04.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Szabadtéren megterített hosszú asztal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-05.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Masă lungă pregătită în aer liber'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-05.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Long table set for an outdoor meal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-05.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Világos vendégszoba több különálló ággyal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-06.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră luminoasă cu mai multe paturi separate'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-06.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Bright guest room with several separate beds'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-06.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Vendégszoba franciaággyal és egyszemélyes ággyal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-07.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu pat dublu și pat de o persoană'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-07.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Guest room with a double bed and a single bed'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-07.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Tetőtéri fürdőszoba zuhanykabinnal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-08.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie la mansardă cu cabină de duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-08.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Attic bathroom with a shower enclosure'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-08.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Fagerendás vendégszoba franciaággyal és egyszemélyes ággyal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-09.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu grinzi din lemn, pat dublu și pat de o persoană'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-09.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Wood-beamed guest room with a double bed and a single bed'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-09.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Fagerendás szoba franciaággyal és ülősarokkal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-10.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu grinzi din lemn, pat dublu și zonă de ședere'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-10.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Wood-beamed room with a double bed and seating area'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-10.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Barna csempés fürdőszoba zuhanykabinnal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-11.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu faianță maro și cabină de duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-11.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Brown-tiled bathroom with a shower enclosure'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-11.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Faborítású vendégszoba franciaággyal és egyszemélyes ággyal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-12.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu finisaje din lemn, pat dublu și pat de o persoană'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-12.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Wood-finished guest room with a double bed and a single bed'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-12.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Vendégszoba franciaággyal, asztallal és televízióval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-13.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu pat dublu, masă și televizor'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-13.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Guest room with a double bed, table, and television'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-13.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Piros-fehér csempés fürdőszoba zuhanykabinnal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-14.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu faianță roșie și albă și cabină de duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-14.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Red-and-white tiled bathroom with a shower enclosure'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-14.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Fa mennyezetű étkező megterített asztallal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-15.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Sală de mese cu tavan din lemn și masă pregătită'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-15.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Wood-ceilinged dining room with a set table'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-15.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Tágas étkező több asztallal és székekkel'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-16.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Sală de mese spațioasă cu mai multe mese și scaune'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-16.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Spacious dining room with several tables and chairs'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-16.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Fa mennyezetű társalgó kanapéval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-17.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Salon cu tavan din lemn și canapea'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-17.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Wood-ceilinged lounge with a sofa'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-17.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Népi szőttesekkel díszített, többágyas tetőtéri szoba'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-18.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră mansardată cu mai multe paturi, decorată cu țesături tradiționale'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-18.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Multi-bed attic room decorated with traditional woven textiles'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-18.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Tetőtéri vendégszoba egyszemélyes ágyakkal és népi szőttesekkel'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-19.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră mansardată cu paturi de o persoană și țesături tradiționale'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-19.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Attic guest room with single beds and traditional woven textiles'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-19.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Faborítású fürdőszoba zuhanyzóval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-20.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu finisaje din lemn și duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-20.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Wood-finished bathroom with a shower'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-20.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Virágokkal és díszkerékkel kialakított kerti bejárat'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-21.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Intrare de grădină decorată cu flori și o roată din lemn'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-21.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Garden entrance decorated with flowers and a wooden wheel'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-21.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Világos, többágyas vendégszoba'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-22.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră luminoasă cu mai multe paturi'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-22.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Bright guest room with several beds'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-22.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Többágyas vendégszoba szekrénnyel és asztallal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-23.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu mai multe paturi, dulap și masă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-23.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Multi-bed guest room with a wardrobe and table'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-23.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Világos fürdőszoba zuhanyzóval és vízmelegítővel'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-24.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie luminoasă cu duș și boiler'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-24.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Bright bathroom with a shower and water heater'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-24.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Fedett terasz kemencével és szabadtéri ülőhellyel'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-25.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Terasă acoperită cu cuptor și locuri de ședere'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-25.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Covered terrace with an outdoor oven and seating'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-25.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Hosszú asztalos, hagyományos tárgyakkal díszített étkező'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-26.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Sală de mese cu masă lungă și obiecte tradiționale'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-26.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Dining room with a long table and traditional decorations'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND image.path = '/images/guesthouses/nisztor/gallery-26.jpg';

INSERT INTO guesthouse_translation (
    guesthouse_id, language_code, name, short_description, description,
    room_description, history_title, history_text
)
SELECT id, 'hu', 'Bukovina Panzió',
       'Tágas, családias szállás 12 szobával, párok, családok és nagyobb társaságok számára.',
       'A Bukovina Panzió 12 rendezett, kényelmes szobával kínál nyugodt szállást Csernakeresztúron. A két-, három- és négyágyas szobák párok, családok és nagyobb társaságok elhelyezésére is alkalmasak; mindegyikhez saját fürdőszoba, televízió, központi fűtés és légkondicionálás tartozik. Minden emeleten felszerelt közös konyha található. A közös szolgáltatások között házi jellegű erdélyi magyaros étkezés egy nagy étkezőben, zárt parkoló, internetkapcsolat, terasz, szabadtéri főzési lehetőség, valamint hidromasszázs- és aeromasszázs-funkciós wellnessdézsa, kültéri zuhanyzó és napozóágyak is szerepelnek. A vendégek pihenéssel tölthetik idejüket, vagy innen indulva felfedezhetik Dél-Erdély történelmi és természeti látnivalóit.',
       'A Bukovina Panzió épületében 12 szoba található: 6 kétágyas szoba, amelyek közül az egyik pótágyazható, 5 háromágyas és 1 négyágyas szoba.',
       'Bukovinai székely örökség Csernakeresztúron',
       'Csernakeresztúr lakosságának több mint fele bukovinai székely gyökerekkel rendelkezik. Őseik az 1764-es madéfalvi veszedelem után Bukovinába menekültek, ahol Istensegíts, Fogadjisten, Hadikfalva, Andrásfalva és Józseffalva közösségeiben telepedtek le. A magyar állam 1911-ben telepített családokat a mai Csernakeresztúrra. Az új közösség 1915–1916-ban templomot épített, 1920-ban pedig katolikus iskolát indított. A bukovinai székely hagyományokat és néptáncokat a helyiek nemzedékről nemzedékre továbbadják.'
FROM guesthouse WHERE slug = 'bukovina-panzio'
ON CONFLICT (guesthouse_id, language_code) DO UPDATE SET
    name = EXCLUDED.name,
    short_description = EXCLUDED.short_description,
    description = EXCLUDED.description,
    room_description = EXCLUDED.room_description,
    history_title = EXCLUDED.history_title,
    history_text = EXCLUDED.history_text;

INSERT INTO guesthouse_translation (
    guesthouse_id, language_code, name, short_description, description,
    room_description, history_title, history_text
)
SELECT id, 'ro', 'Pensiunea Bukovina',
       'O pensiune spațioasă și primitoare, cu 12 camere pentru cupluri, familii și grupuri mai numeroase.',
       'Pensiunea Bukovina oferă cazare liniștită în Cristur, în 12 camere îngrijite și confortabile. Camerele duble, triple și cvadruple sunt potrivite pentru cupluri, familii și grupuri mai numeroase; fiecare cameră are baie proprie, televizor, încălzire centrală și aer condiționat. La fiecare etaj se află o bucătărie comună utilată. Serviciile comune includ preparate ardelenești și ungurești cu caracter tradițional servite într-o sală de mese mare, parcare închisă, internet, terasă, posibilități de gătit în aer liber, precum și un ciubăr wellness cu hidromasaj și aeromasaj, duș exterior și șezlonguri. Oaspeții se pot bucura de odihnă sau pot porni de aici pentru a descoperi atracțiile istorice și naturale ale Transilvaniei de Sud.',
       'Pensiunea Bukovina are 12 camere: 6 camere duble, dintre care una permite un pat suplimentar, 5 camere triple și 1 cameră cvadruplă.',
       'Moștenirea secuilor bucovineni din Cristur',
       'Mai mult de jumătate dintre locuitorii satului Cristur au rădăcini secuiești bucovinene. Strămoșii lor s-au refugiat în Bucovina după evenimentele de la Siculeni din 1764 și s-au așezat în comunitățile Istensegíts, Fogadjisten, Hadikfalva, Andrásfalva și Józseffalva. În 1911, statul maghiar a strămutat familii în actualul Cristur. Noua comunitate a ridicat o biserică în anii 1915–1916 și a deschis o școală catolică în 1920. Tradițiile și dansurile secuilor bucovineni sunt transmise și astăzi din generație în generație.'
FROM guesthouse WHERE slug = 'bukovina-panzio'
ON CONFLICT (guesthouse_id, language_code) DO UPDATE SET
    name = EXCLUDED.name,
    short_description = EXCLUDED.short_description,
    description = EXCLUDED.description,
    room_description = EXCLUDED.room_description,
    history_title = EXCLUDED.history_title,
    history_text = EXCLUDED.history_text;

INSERT INTO guesthouse_translation (
    guesthouse_id, language_code, name, short_description, description,
    room_description, history_title, history_text
)
SELECT id, 'en', 'Bukovina Guesthouse',
       'A spacious, welcoming 12-room guesthouse for couples, families, and larger groups.',
       'Bukovina Guesthouse offers peaceful accommodation in Cristur in 12 tidy, comfortable rooms. Its double, triple, and quadruple rooms suit couples, families, and larger groups; every room has a private bathroom, television, central heating, and air conditioning. Every floor has an equipped shared kitchen. Shared services include homemade Transylvanian-Hungarian meals served in one large dining room, enclosed parking, internet access, a terrace, outdoor cooking facilities, and a wellness tub with hydro- and air massage, an outdoor shower, and sun loungers. Guests can spend their time relaxing or use the guesthouse as a base for exploring the historic and natural sights of Southern Transylvania.',
       'Bukovina Guesthouse has 12 rooms: 6 double rooms, one of which can take an extra bed, 5 triple rooms, and 1 quadruple room.',
       'Bukovina Szekler heritage in Cristur',
       'More than half of Cristur''s residents have Bukovina Szekler roots. Their ancestors fled to Bukovina after the 1764 events at Siculeni and settled in the communities of Istensegíts, Fogadjisten, Hadikfalva, Andrásfalva, and Józseffalva. In 1911, the Hungarian state resettled families in present-day Cristur. The new community built a church in 1915–1916 and opened a Catholic school in 1920. Bukovina Szekler traditions and folk dances continue to be passed down from one generation to the next.'
FROM guesthouse WHERE slug = 'bukovina-panzio'
ON CONFLICT (guesthouse_id, language_code) DO UPDATE SET
    name = EXCLUDED.name,
    short_description = EXCLUDED.short_description,
    description = EXCLUDED.description,
    room_description = EXCLUDED.room_description,
    history_title = EXCLUDED.history_title,
    history_text = EXCLUDED.history_text;

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Csoport a Bukovina Panzió épülete előtt'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-01.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Grup în fața clădirii Pensiunii Bukovina'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-01.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Group in front of the Bukovina Guesthouse building'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-01.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Két személy a kőburkolatú panzió előtt'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-02.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Două persoane în fața pensiunii placate cu piatră'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-02.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Two people in front of the stone-clad guesthouse'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-02.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'A Bukovina Panzió udvara fedett terasszal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-03.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Curtea Pensiunii Bukovina cu terasă acoperită'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-03.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Bukovina Guesthouse courtyard with a covered terrace'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-03.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'A Bukovina Panzió utcai homlokzata'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-04.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Fațada stradală a Pensiunii Bukovina'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-04.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Street-facing façade of Bukovina Guesthouse'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-04.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'A Bukovina Panzió oldalsó homlokzata és bejárata'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-05.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Fațada laterală și intrarea Pensiunii Bukovina'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-05.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Side façade and entrance of Bukovina Guesthouse'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-05.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Virágokkal díszített fedett bejárat'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-06.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Intrare acoperită decorată cu flori'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-06.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Covered entrance decorated with flowers'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-06.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Kerek tükörben tükröződő franciaágy'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-07.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Pat dublu reflectat într-o oglindă rotundă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-07.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Double bed reflected in a round mirror'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-07.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Elegáns vendégszoba fehér franciaággyal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-08.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră elegantă cu pat dublu alb'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-08.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Elegant guest room with a white double bed'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-08.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Szürke csempés fürdőszoba zuhanykabinnal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-09.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu faianță gri și cabină de duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-09.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Grey-tiled bathroom with a shower enclosure'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-09.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Megterített asztal mögött látható kétágyas szoba'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-10.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu două paturi văzută din spatele unei mese pregătite'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-10.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Twin room viewed past a set table'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-10.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Világos kétágyas szoba székekkel'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-11.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră luminoasă cu două paturi și scaune'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-11.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Bright twin room with chairs'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-11.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Mintás falú vendégszoba fa franciaággyal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-12.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu perete decorativ și pat dublu din lemn'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-12.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Guest room with a patterned feature wall and wooden double bed'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-12.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Fa franciaágyas szoba ülősarokkal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-13.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu pat dublu din lemn și zonă de ședere'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-13.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Room with a wooden double bed and seating area'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-13.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Barna csempés fürdőszoba zuhanyzóval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-14.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu faianță maro și duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-14.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Brown-tiled bathroom with a shower'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-14.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Világos franciaágyas szoba kanapéval és televízióval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-15.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră luminoasă cu pat dublu, canapea și televizor'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-15.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Bright double room with a sofa and television'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-15.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Szürke-fehér fürdőszoba üvegfalú zuhanyzóval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-16.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie gri și albă cu duș cu perete din sticlă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-16.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Grey-and-white bathroom with a glass-screen shower'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-16.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Virágmintás falú szoba fa franciaággyal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-17.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu perete floral și pat dublu din lemn'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-17.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Room with a floral feature wall and wooden double bed'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-17.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Meleg tónusú franciaágyas szoba saját fürdőszobával'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-18.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră în tonuri calde, cu pat dublu și baie proprie'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-18.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Warm-toned double room with an en-suite bathroom'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-18.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Lila-fehér csempés fürdőszoba zuhanyzóval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-19.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu faianță mov și albă și duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-19.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Purple-and-white tiled bathroom with a shower'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-19.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Franciaágyas szoba televízióval és asztallal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-20.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu pat dublu, televizor și masă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-20.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Double room with a television and table'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-20.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Tágas franciaágyas szoba televízióval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-21.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră dublă spațioasă cu televizor'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-21.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Spacious double room with a television'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-21.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Két különálló ágyas szoba asztallal és televízióval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-22.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu două paturi separate, masă și televizor'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-22.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Twin room with a table and television'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-22.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Franciaágyas szoba kanapéval és virágmintás fallal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-23.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu pat dublu, canapea și perete floral'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-23.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Double room with a sofa and floral feature wall'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-23.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Franciaágyas és egyszemélyes ágyas szoba'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-24.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu pat dublu și pat de o persoană'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-24.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Room with a double bed and a single bed'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-24.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Fa franciaágyas szoba nagy ablakkal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-25.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu pat dublu din lemn și fereastră mare'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-25.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Room with a wooden double bed and large window'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-25.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Piros-fehér csempés fürdőszoba káddal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-26.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu faianță roșie și albă și cadă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-26.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Red-and-white tiled bathroom with a bathtub'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-26.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Étkezőasztal virágdísszel és konyhasarokkal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-27.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Masă de dining cu flori și chicinetă'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-27.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Dining table with flowers and a kitchenette'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-27.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Közös konyhai és étkezőfolyosó'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-28.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Spațiu comun de bucătărie și dining pe hol'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-28.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Shared kitchen and dining hallway'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-28.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Virágmintás falú szoba franciaággyal és kanapéval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-29.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră cu perete floral, pat dublu și canapea'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-29.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Room with a floral wall, double bed, and sofa'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-29.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Tetőtéri szoba több ággyal és ülősarokkal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-30.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră mansardată cu mai multe paturi și zonă de ședere'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-30.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Attic room with several beds and a seating area'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-30.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Tetőtéri franciaágyas szoba kanapéval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-31.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră mansardată cu pat dublu și canapea'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-31.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Attic double room with a sofa'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-31.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Tetőtéri szoba franciaággyal, egyszemélyes ággyal és kanapéval'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-32.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Cameră mansardată cu pat dublu, pat de o persoană și canapea'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-32.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Attic room with a double bed, single bed, and sofa'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-32.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Kis konyha étkezősarokkal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-33.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Bucătărie mică cu zonă de luat masa'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-33.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Small kitchen with a dining nook'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-33.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'hu', 'Kék-fehér csempés fürdőszoba zuhanykabinnal'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-34.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'ro', 'Baie cu faianță albastră și albă și cabină de duș'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-34.jpg';

INSERT INTO guesthouse_image_translation (image_id, language_code, alt_text)
SELECT image.id, 'en', 'Blue-and-white tiled bathroom with a shower enclosure'
FROM guesthouse_image image
JOIN guesthouse ON guesthouse.id = image.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND image.path = '/images/guesthouses/bukovina/gallery-34.jpg';

ALTER TABLE guesthouse_translation
    ALTER COLUMN history_title SET NOT NULL,
    ALTER COLUMN history_text SET NOT NULL;

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'person_1', 'PERSON', 'Nisztor István',
       FALSE, TRUE, 0
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Kapcsolattartó'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'person_1';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Persoană de contact'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'person_1';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Contact person'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'person_1';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'person_2', 'PERSON', 'Nisztor Éva',
       FALSE, TRUE, 1
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Kapcsolattartó'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'person_2';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Persoană de contact'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'person_2';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Contact person'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'person_2';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'landline_fax', 'PHONE', '+40 254 236 172',
       FALSE, TRUE, 2
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Telefon és fax'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'landline_fax';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Telefon și fax'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'landline_fax';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Phone and fax'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'landline_fax';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'mobile_primary', 'PHONE', '+40 743 677 812',
       TRUE, TRUE, 3
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Elsődleges mobiltelefon'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'mobile_primary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Telefon mobil principal'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'mobile_primary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Primary mobile'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'mobile_primary';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'mobile_secondary', 'PHONE', '+40 744 198 744',
       FALSE, TRUE, 4
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'További mobiltelefon'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'mobile_secondary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Telefon mobil suplimentar'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'mobile_secondary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Additional mobile'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'mobile_secondary';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'email', 'EMAIL', 'nisztorpanzio@gmail.com',
       FALSE, TRUE, 5
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'E-mail'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'email';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'E-mail'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'email';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Email'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND contact.code = 'email';

INSERT INTO guesthouse_address (guesthouse_id, latitude, longitude, active)
SELECT id, 45.82361, 22.93869, TRUE
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_address_translation (address_id, language_code, formatted_address)
SELECT address.id, 'hu', '330003 Csernakeresztúr, Bucovina utca 17., Hunyad megye, Románia'
FROM guesthouse_address address
JOIN guesthouse ON guesthouse.id = address.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO guesthouse_address_translation (address_id, language_code, formatted_address)
SELECT address.id, 'ro', 'Strada Bucovina nr. 17, Cristur 330003, județul Hunedoara, România'
FROM guesthouse_address address
JOIN guesthouse ON guesthouse.id = address.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO guesthouse_address_translation (address_id, language_code, formatted_address)
SELECT address.id, 'en', '17 Bucovina Street, Cristur 330003, Hunedoara County, Romania'
FROM guesthouse_address address
JOIN guesthouse ON guesthouse.id = address.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'person_1', 'PERSON', 'Nisztor István',
       FALSE, TRUE, 0
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Kapcsolattartó'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'person_1';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Persoană de contact'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'person_1';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Contact person'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'person_1';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'person_2', 'PERSON', 'Nisztor Éva',
       FALSE, TRUE, 1
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Kapcsolattartó'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'person_2';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Persoană de contact'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'person_2';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Contact person'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'person_2';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'landline_fax', 'PHONE', '+40 254 236 172',
       FALSE, TRUE, 2
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Telefon és fax'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'landline_fax';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Telefon și fax'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'landline_fax';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Phone and fax'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'landline_fax';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'mobile_primary', 'PHONE', '+40 743 677 812',
       TRUE, TRUE, 3
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'Elsődleges mobiltelefon'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'mobile_primary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Telefon mobil principal'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'mobile_primary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Primary mobile'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'mobile_primary';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'mobile_secondary', 'PHONE', '+40 744 198 744',
       FALSE, TRUE, 4
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'További mobiltelefon'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'mobile_secondary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'Telefon mobil suplimentar'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'mobile_secondary';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Additional mobile'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'mobile_secondary';

INSERT INTO guesthouse_contact (
    guesthouse_id, code, type, value, preferred, active, display_order
)
SELECT id, 'email', 'EMAIL', 'nisztorpanzio@gmail.com',
       FALSE, TRUE, 5
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'hu', 'E-mail'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'email';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'ro', 'E-mail'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'email';

INSERT INTO guesthouse_contact_translation (contact_id, language_code, label)
SELECT contact.id, 'en', 'Email'
FROM guesthouse_contact contact
JOIN guesthouse ON guesthouse.id = contact.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND contact.code = 'email';

INSERT INTO guesthouse_address (guesthouse_id, latitude, longitude, active)
SELECT id, 45.82361, 22.93869, TRUE
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_address_translation (address_id, language_code, formatted_address)
SELECT address.id, 'hu', '330003 Csernakeresztúr, Bucovina utca 17., Hunyad megye, Románia'
FROM guesthouse_address address
JOIN guesthouse ON guesthouse.id = address.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO guesthouse_address_translation (address_id, language_code, formatted_address)
SELECT address.id, 'ro', 'Strada Bucovina nr. 17, Cristur 330003, județul Hunedoara, România'
FROM guesthouse_address address
JOIN guesthouse ON guesthouse.id = address.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO guesthouse_address_translation (address_id, language_code, formatted_address)
SELECT address.id, 'en', '17 Bucovina Street, Cristur 330003, Hunedoara County, Romania'
FROM guesthouse_address address
JOIN guesthouse ON guesthouse.id = address.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO amenity (code, category)
VALUES ('private_parking', 'OUTDOOR_WELLNESS');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Zárt parkoló',
       NULL
FROM amenity WHERE code = 'private_parking';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Parcare privată închisă',
       NULL
FROM amenity WHERE code = 'private_parking';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Enclosed private parking',
       NULL
FROM amenity WHERE code = 'private_parking';

INSERT INTO amenity (code, category)
VALUES ('wifi', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Vezeték nélküli internet',
       NULL
FROM amenity WHERE code = 'wifi';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Internet Wi-Fi',
       NULL
FROM amenity WHERE code = 'wifi';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Wi-Fi internet',
       NULL
FROM amenity WHERE code = 'wifi';

INSERT INTO amenity (code, category)
VALUES ('private_bathroom', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Saját fürdőszoba minden szobához',
       NULL
FROM amenity WHERE code = 'private_bathroom';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Baie proprie pentru fiecare cameră',
       NULL
FROM amenity WHERE code = 'private_bathroom';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Private bathroom for every room',
       NULL
FROM amenity WHERE code = 'private_bathroom';

INSERT INTO amenity (code, category)
VALUES ('television', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Televízió minden szobában',
       NULL
FROM amenity WHERE code = 'television';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Televizor în fiecare cameră',
       NULL
FROM amenity WHERE code = 'television';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Television in every room',
       NULL
FROM amenity WHERE code = 'television';

INSERT INTO amenity (code, category)
VALUES ('central_heating', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Központi fűtés minden szobában',
       NULL
FROM amenity WHERE code = 'central_heating';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Încălzire centrală în fiecare cameră',
       NULL
FROM amenity WHERE code = 'central_heating';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Central heating in every room',
       NULL
FROM amenity WHERE code = 'central_heating';

INSERT INTO amenity (code, category)
VALUES ('air_conditioning', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Légkondicionálás minden szobában',
       NULL
FROM amenity WHERE code = 'air_conditioning';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Aer condiționat în fiecare cameră',
       NULL
FROM amenity WHERE code = 'air_conditioning';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Air conditioning in every room',
       NULL
FROM amenity WHERE code = 'air_conditioning';

INSERT INTO amenity (code, category)
VALUES ('refrigerator', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Hűtőszekrény',
       NULL
FROM amenity WHERE code = 'refrigerator';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Frigider',
       NULL
FROM amenity WHERE code = 'refrigerator';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Refrigerator',
       NULL
FROM amenity WHERE code = 'refrigerator';

INSERT INTO amenity (code, category)
VALUES ('floor_kitchens', 'FOOD_KITCHEN');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Felszerelt közös konyha minden emeleten',
       'Minden emeleten egy-egy közös konyha található mikrohullámú sütővel, kenyérpirítóval és vízforralóval.'
FROM amenity WHERE code = 'floor_kitchens';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Bucătărie comună utilată la fiecare etaj',
       'La fiecare etaj se află câte o bucătărie comună, dotată cu cuptor cu microunde, prăjitor de pâine și fierbător de apă.'
FROM amenity WHERE code = 'floor_kitchens';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Equipped shared kitchen on every floor',
       'Each floor has a shared kitchen equipped with a microwave oven, toaster, and electric kettle.'
FROM amenity WHERE code = 'floor_kitchens';

INSERT INTO amenity (code, category)
VALUES ('sandwich_maker', 'FOOD_KITCHEN');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Szendvicssütő',
       NULL
FROM amenity WHERE code = 'sandwich_maker';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Aparat pentru sandvișuri',
       NULL
FROM amenity WHERE code = 'sandwich_maker';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Sandwich maker',
       NULL
FROM amenity WHERE code = 'sandwich_maker';

INSERT INTO amenity (code, category)
VALUES ('iron', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Vasaló és vasalódeszka',
       NULL
FROM amenity WHERE code = 'iron';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Fier și masă de călcat',
       NULL
FROM amenity WHERE code = 'iron';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Iron and ironing board',
       NULL
FROM amenity WHERE code = 'iron';

INSERT INTO amenity (code, category)
VALUES ('drying_rack', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Ruhaszárító',
       NULL
FROM amenity WHERE code = 'drying_rack';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Uscător de rufe',
       NULL
FROM amenity WHERE code = 'drying_rack';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Clothes drying rack',
       NULL
FROM amenity WHERE code = 'drying_rack';

INSERT INTO amenity (code, category)
VALUES ('hairdryer', 'ROOM_COMFORT');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Hajszárító',
       NULL
FROM amenity WHERE code = 'hairdryer';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Uscător de păr',
       NULL
FROM amenity WHERE code = 'hairdryer';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Hairdryer',
       NULL
FROM amenity WHERE code = 'hairdryer';

INSERT INTO amenity (code, category)
VALUES ('dining_air_conditioning', 'FOOD_KITCHEN');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Légkondicionált étkező',
       NULL
FROM amenity WHERE code = 'dining_air_conditioning';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Sală de mese cu aer condiționat',
       NULL
FROM amenity WHERE code = 'dining_air_conditioning';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Air-conditioned dining area',
       NULL
FROM amenity WHERE code = 'dining_air_conditioning';

INSERT INTO amenity (code, category)
VALUES ('outdoor_cooking', 'OUTDOOR_WELLNESS');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Grillező, bogrács és kemence',
       NULL
FROM amenity WHERE code = 'outdoor_cooking';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Grătar, ceaun și cuptor exterior',
       NULL
FROM amenity WHERE code = 'outdoor_cooking';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Barbecue, cauldron cooking, and outdoor oven',
       NULL
FROM amenity WHERE code = 'outdoor_cooking';

INSERT INTO amenity (code, category)
VALUES ('terrace', 'OUTDOOR_WELLNESS');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Terasz',
       NULL
FROM amenity WHERE code = 'terrace';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Terasă',
       NULL
FROM amenity WHERE code = 'terrace';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Terrace',
       NULL
FROM amenity WHERE code = 'terrace';

INSERT INTO amenity (code, category)
VALUES ('dining_room', 'FOOD_KITCHEN');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Nagy, közös étkező minden vendég számára',
       'A panziók vendégeit egy nagy, közös étkező fogadja.'
FROM amenity WHERE code = 'dining_room';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Sală de mese mare, comună, pentru toți oaspeții',
       'Oaspeții pensiunilor au la dispoziție o sală de mese mare și comună.'
FROM amenity WHERE code = 'dining_room';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Large shared dining room for all guests',
       'Guests of the guesthouses have access to one large shared dining room.'
FROM amenity WHERE code = 'dining_room';

INSERT INTO amenity (code, category)
VALUES ('playground', 'OUTDOOR_WELLNESS');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Játszótér',
       NULL
FROM amenity WHERE code = 'playground';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Loc de joacă',
       NULL
FROM amenity WHERE code = 'playground';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Playground',
       NULL
FROM amenity WHERE code = 'playground';

INSERT INTO amenity (code, category)
VALUES ('sun_loungers', 'OUTDOOR_WELLNESS');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Napozóágyak',
       'A wellnessdézsa mellett napozóágyak állnak a vendégek rendelkezésére.'
FROM amenity WHERE code = 'sun_loungers';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Șezlonguri',
       'Lângă ciubărul de relaxare sunt disponibile șezlonguri pentru oaspeți.'
FROM amenity WHERE code = 'sun_loungers';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Sun loungers',
       'Sun loungers are available beside the wellness tub.'
FROM amenity WHERE code = 'sun_loungers';

INSERT INTO amenity (code, category)
VALUES ('wellness_tub', 'OUTDOOR_WELLNESS');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Wellnessdézsa hidromasszázzsal és aeromasszázzsal',
       'A dézsa hidromasszázs- és aeromasszázs-funkcióval rendelkezik; mellette napozóágyak és kültéri zuhanyzó található.'
FROM amenity WHERE code = 'wellness_tub';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Ciubăr wellness cu hidromasaj și aeromasaj',
       'Ciubărul dispune de funcții de hidromasaj și aeromasaj, iar în apropiere se află șezlonguri și un duș exterior.'
FROM amenity WHERE code = 'wellness_tub';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Wellness tub with hydro- and air massage',
       'The tub offers hydro- and air-massage functions, with sun loungers and an outdoor shower beside it.'
FROM amenity WHERE code = 'wellness_tub';

INSERT INTO amenity (code, category)
VALUES ('games', 'PROGRAM_GROUP');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Társas- és kártyajátékok',
       NULL
FROM amenity WHERE code = 'games';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Jocuri de societate și de cărți',
       NULL
FROM amenity WHERE code = 'games';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Board and card games',
       NULL
FROM amenity WHERE code = 'games';

INSERT INTO amenity (code, category)
VALUES ('homemade_meals', 'FOOD_KITCHEN');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Házi, erdélyi magyaros ételek',
       'Hagyományos jellegű ételek és italok, nagyrészt saját előállítású alapanyagokból.'
FROM amenity WHERE code = 'homemade_meals';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Preparate de casă, ardelenești și ungurești',
       'Mâncăruri și băuturi tradiționale, pregătite în mare parte din ingrediente produse în gospodărie.'
FROM amenity WHERE code = 'homemade_meals';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Homemade Transylvanian-Hungarian food',
       'Traditional-style food and drinks, prepared largely from ingredients produced by the hosts.'
FROM amenity WHERE code = 'homemade_meals';

INSERT INTO amenity (code, category)
VALUES ('meal_plans', 'FOOD_KITCHEN');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Reggeli, félpanzió és teljes ellátás',
       NULL
FROM amenity WHERE code = 'meal_plans';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Mic dejun, demipensiune și pensiune completă',
       NULL
FROM amenity WHERE code = 'meal_plans';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Breakfast, half-board, and full-board options',
       NULL
FROM amenity WHERE code = 'meal_plans';

INSERT INTO amenity (code, category)
VALUES ('programme_planning', 'PROGRAM_GROUP');

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'hu', 'Program- és csillagtúra-szervezési segítség',
       NULL
FROM amenity WHERE code = 'programme_planning';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'ro', 'Asistență pentru programe și excursii în circuit',
       NULL
FROM amenity WHERE code = 'programme_planning';

INSERT INTO amenity_translation (amenity_id, language_code, name, description)
SELECT id, 'en', 'Programme and day-trip planning assistance',
       NULL
FROM amenity WHERE code = 'programme_planning';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 0
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'private_parking';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 1
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'wifi';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 2
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'private_bathroom';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 3
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'television';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 4
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'central_heating';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 5
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'air_conditioning';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 6
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'refrigerator';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 7
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'floor_kitchens';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 8
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'sandwich_maker';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 9
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'iron';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 10
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'drying_rack';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 11
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'hairdryer';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 12
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'dining_air_conditioning';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 13
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'outdoor_cooking';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 14
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'terrace';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 15
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'dining_room';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 16
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'playground';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 17
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'sun_loungers';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 18
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'wellness_tub';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 19
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'games';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 20
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'homemade_meals';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 21
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'meal_plans';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 22
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND amenity.code = 'programme_planning';

INSERT INTO room_type (
    guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed,
    extra_beds_per_eligible_room, active, display_order
)
SELECT id, 'double', 3, 2,
       1,
       1, TRUE, 0
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'hu', 'Kétágyas szoba'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'double';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'ro', 'Cameră dublă'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'double';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'en', 'Double room'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'double';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 0
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'private_bathroom';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 1
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'television';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 2
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'central_heating';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 3
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'air_conditioning';

INSERT INTO room_type (
    guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed,
    extra_beds_per_eligible_room, active, display_order
)
SELECT id, 'triple', 1, 3,
       0,
       0, TRUE, 1
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'hu', 'Háromágyas szoba'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'triple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'ro', 'Cameră triplă'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'triple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'en', 'Triple room'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'triple';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 0
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'private_bathroom';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 1
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'television';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 2
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'central_heating';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 3
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'air_conditioning';

INSERT INTO room_type (
    guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed,
    extra_beds_per_eligible_room, active, display_order
)
SELECT id, 'quadruple', 1, 4,
       0,
       0, TRUE, 2
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'hu', 'Négyágyas szoba'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'quadruple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'ro', 'Cameră cvadruplă'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'quadruple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'en', 'Quadruple room'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'quadruple';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 0
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'private_bathroom';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 1
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'television';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 2
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'central_heating';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 3
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'nisztor-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'air_conditioning';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 0
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'private_parking';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 1
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'wifi';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 2
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'private_bathroom';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 3
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'television';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 4
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'central_heating';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 5
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'air_conditioning';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 6
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'refrigerator';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 7
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'floor_kitchens';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 8
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'sandwich_maker';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 9
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'iron';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 10
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'drying_rack';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 11
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'hairdryer';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 12
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'dining_air_conditioning';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 13
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'outdoor_cooking';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 14
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'terrace';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 15
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'dining_room';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 16
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'playground';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 17
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'sun_loungers';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 18
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'wellness_tub';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 19
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'games';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 20
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'homemade_meals';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 21
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'meal_plans';

INSERT INTO guesthouse_amenity (guesthouse_id, amenity_id, active, display_order)
SELECT guesthouse.id, amenity.id, TRUE, 22
FROM guesthouse CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND amenity.code = 'programme_planning';

INSERT INTO room_type (
    guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed,
    extra_beds_per_eligible_room, active, display_order
)
SELECT id, 'double', 6, 2,
       1,
       1, TRUE, 0
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'hu', 'Kétágyas szoba'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'double';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'ro', 'Cameră dublă'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'double';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'en', 'Double room'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'double';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 0
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'private_bathroom';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 1
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'television';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 2
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'central_heating';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 3
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'double'
  AND amenity.code = 'air_conditioning';

INSERT INTO room_type (
    guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed,
    extra_beds_per_eligible_room, active, display_order
)
SELECT id, 'triple', 5, 3,
       0,
       0, TRUE, 1
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'hu', 'Háromágyas szoba'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'triple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'ro', 'Cameră triplă'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'triple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'en', 'Triple room'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'triple';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 0
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'private_bathroom';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 1
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'television';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 2
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'central_heating';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 3
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'triple'
  AND amenity.code = 'air_conditioning';

INSERT INTO room_type (
    guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed,
    extra_beds_per_eligible_room, active, display_order
)
SELECT id, 'quadruple', 1, 4,
       0,
       0, TRUE, 2
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'hu', 'Négyágyas szoba'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'quadruple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'ro', 'Cameră cvadruplă'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'quadruple';

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id, 'en', 'Quadruple room'
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'quadruple';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 0
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'private_bathroom';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 1
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'television';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 2
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'central_heating';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT room_type.id, amenity.id, 3
FROM room_type
JOIN guesthouse ON guesthouse.id = room_type.guesthouse_id
CROSS JOIN amenity
WHERE guesthouse.slug = 'bukovina-panzio'
  AND room_type.code = 'quadruple'
  AND amenity.code = 'air_conditioning';

INSERT INTO guesthouse_pricing (guesthouse_id, currency, active)
SELECT id, 'RON', TRUE
FROM guesthouse WHERE slug = 'nisztor-panzio';

INSERT INTO guesthouse_pricing_translation (pricing_id, language_code, payment_note)
SELECT pricing.id, 'hu', 'Az árak forintra vagy euróra is átszámíthatók a napi árfolyamon; a forrásoldal szerint ezekben a pénznemekben is lehet fizetni.'
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO guesthouse_pricing_translation (pricing_id, language_code, payment_note)
SELECT pricing.id, 'ro', 'Tarifele pot fi convertite în forinți sau euro la cursul zilei; potrivit paginii-sursă, plata este posibilă și în aceste monede.'
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO guesthouse_pricing_translation (pricing_id, language_code, payment_note)
SELECT pricing.id, 'en', 'Rates may be converted to Hungarian forints or euros at the daily exchange rate; according to the source page, payment may also be made in these currencies.'
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'accommodation', 130,
       'person_night', TRUE, 0
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'accommodation';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'accommodation';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Accommodation'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'accommodation';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'single_occupancy_room', 200,
       'person_night', TRUE, 1
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Egyágyas használat'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'single_occupancy_room';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cameră în regim single'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'single_occupancy_room';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Single occupancy'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'single_occupancy_room';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'breakfast', 45,
       'person', TRUE, 2
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Reggeli'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Mic dejun'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Breakfast'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'breakfast';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'lunch', 75,
       'person', TRUE, 3
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Ebéd'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'lunch';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Prânz'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'lunch';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Lunch'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'lunch';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'dinner', 75,
       'person', TRUE, 4
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Vacsora'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'dinner';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cină'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'dinner';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Dinner'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'dinner';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'bed_and_breakfast', 175,
       'person_night', TRUE, 5
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás reggelivel'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'bed_and_breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare cu mic dejun'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'bed_and_breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Bed and breakfast'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'bed_and_breakfast';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'half_board', 250,
       'person_night', TRUE, 6
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás félpanzióval'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'half_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare cu demipensiune'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'half_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Accommodation with half-board'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'half_board';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'full_board', 325,
       'person_night', TRUE, 7
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás teljes ellátással'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'full_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare cu pensiune completă'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'full_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Accommodation with full-board'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'full_board';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'tour_guide', 600,
       'day', TRUE, 8
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Idegenvezetés'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'tour_guide';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Servicii de ghidaj'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'tour_guide';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Tour guide service'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND item.code = 'tour_guide';

INSERT INTO pricing_adjustment (
    pricing_id, code, kind, percentage, active, display_order
)
SELECT pricing.id, 'tourist_tax', 'SURCHARGE',
       1, TRUE, 0
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'hu', 'Idegenforgalmi adó'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'tourist_tax';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'ro', 'Taxă turistică'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'tourist_tax';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'en', 'Tourist tax'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'tourist_tax';

INSERT INTO pricing_adjustment (
    pricing_id, code, kind, percentage, active, display_order
)
SELECT pricing.id, 'coach_group', 'DISCOUNT',
       10, TRUE, 1
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'hu', 'Buszos csoport kedvezménye'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'coach_group';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'ro', 'Reducere pentru grupuri cu autocarul'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'coach_group';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'en', 'Coach-group discount'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'coach_group';

INSERT INTO pricing_adjustment (
    pricing_id, code, kind, percentage, active, display_order
)
SELECT pricing.id, 'children_under_10', 'DISCOUNT',
       25, TRUE, 2
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'hu', '10 év alatti gyermekek kedvezménye'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'children_under_10';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'ro', 'Reducere pentru copiii sub 10 ani'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'children_under_10';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'en', 'Discount for children under 10'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'nisztor-panzio'
  AND adjustment.code = 'children_under_10';

INSERT INTO guesthouse_pricing (guesthouse_id, currency, active)
SELECT id, 'RON', TRUE
FROM guesthouse WHERE slug = 'bukovina-panzio';

INSERT INTO guesthouse_pricing_translation (pricing_id, language_code, payment_note)
SELECT pricing.id, 'hu', 'Az árak forintra vagy euróra is átszámíthatók a napi árfolyamon; a forrásoldal szerint ezekben a pénznemekben is lehet fizetni.'
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO guesthouse_pricing_translation (pricing_id, language_code, payment_note)
SELECT pricing.id, 'ro', 'Tarifele pot fi convertite în forinți sau euro la cursul zilei; potrivit paginii-sursă, plata este posibilă și în aceste monede.'
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO guesthouse_pricing_translation (pricing_id, language_code, payment_note)
SELECT pricing.id, 'en', 'Rates may be converted to Hungarian forints or euros at the daily exchange rate; according to the source page, payment may also be made in these currencies.'
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'accommodation', 130,
       'person_night', TRUE, 0
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'accommodation';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'accommodation';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Accommodation'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'accommodation';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'single_occupancy_room', 200,
       'person_night', TRUE, 1
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Egyágyas használat'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'single_occupancy_room';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cameră în regim single'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'single_occupancy_room';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Single occupancy'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'single_occupancy_room';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'breakfast', 45,
       'person', TRUE, 2
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Reggeli'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Mic dejun'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Breakfast'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'breakfast';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'lunch', 75,
       'person', TRUE, 3
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Ebéd'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'lunch';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Prânz'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'lunch';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Lunch'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'lunch';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'dinner', 75,
       'person', TRUE, 4
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Vacsora'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'dinner';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cină'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'dinner';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Dinner'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'dinner';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'bed_and_breakfast', 175,
       'person_night', TRUE, 5
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás reggelivel'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'bed_and_breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare cu mic dejun'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'bed_and_breakfast';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Bed and breakfast'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'bed_and_breakfast';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'half_board', 250,
       'person_night', TRUE, 6
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás félpanzióval'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'half_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare cu demipensiune'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'half_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Accommodation with half-board'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'half_board';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'full_board', 325,
       'person_night', TRUE, 7
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Szállás teljes ellátással'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'full_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Cazare cu pensiune completă'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'full_board';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Accommodation with full-board'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'full_board';

INSERT INTO price_item (pricing_id, code, amount, unit, active, display_order)
SELECT pricing.id, 'tour_guide', 600,
       'day', TRUE, 8
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'hu', 'Idegenvezetés'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'tour_guide';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'ro', 'Servicii de ghidaj'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'tour_guide';

INSERT INTO price_item_translation (price_item_id, language_code, label)
SELECT item.id, 'en', 'Tour guide service'
FROM price_item item
JOIN guesthouse_pricing pricing ON pricing.id = item.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND item.code = 'tour_guide';

INSERT INTO pricing_adjustment (
    pricing_id, code, kind, percentage, active, display_order
)
SELECT pricing.id, 'tourist_tax', 'SURCHARGE',
       1, TRUE, 0
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'hu', 'Idegenforgalmi adó'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'tourist_tax';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'ro', 'Taxă turistică'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'tourist_tax';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'en', 'Tourist tax'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'tourist_tax';

INSERT INTO pricing_adjustment (
    pricing_id, code, kind, percentage, active, display_order
)
SELECT pricing.id, 'coach_group', 'DISCOUNT',
       10, TRUE, 1
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'hu', 'Buszos csoport kedvezménye'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'coach_group';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'ro', 'Reducere pentru grupuri cu autocarul'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'coach_group';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'en', 'Coach-group discount'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'coach_group';

INSERT INTO pricing_adjustment (
    pricing_id, code, kind, percentage, active, display_order
)
SELECT pricing.id, 'children_under_10', 'DISCOUNT',
       25, TRUE, 2
FROM guesthouse_pricing pricing
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'hu', '10 év alatti gyermekek kedvezménye'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'children_under_10';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'ro', 'Reducere pentru copiii sub 10 ani'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'children_under_10';

INSERT INTO pricing_adjustment_translation (adjustment_id, language_code, label)
SELECT adjustment.id, 'en', 'Discount for children under 10'
FROM pricing_adjustment adjustment
JOIN guesthouse_pricing pricing ON pricing.id = adjustment.pricing_id
JOIN guesthouse ON guesthouse.id = pricing.guesthouse_id
WHERE guesthouse.slug = 'bukovina-panzio'
  AND adjustment.code = 'children_under_10';
