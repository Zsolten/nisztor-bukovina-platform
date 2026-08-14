ALTER TABLE guesthouse_translation
    ADD COLUMN story_eyebrow VARCHAR(240),
    ADD COLUMN story_title VARCHAR(240),
    ADD COLUMN dining_eyebrow VARCHAR(240),
    ADD COLUMN dining_title VARCHAR(240),
    ADD COLUMN dining_description VARCHAR(1000),
    ADD COLUMN amenities_title VARCHAR(240),
    ADD COLUMN room_types_title VARCHAR(240),
    ADD COLUMN pricing_title VARCHAR(240),
    ADD COLUMN history_eyebrow VARCHAR(240),
    ADD COLUMN gallery_title VARCHAR(240),
    ADD COLUMN gallery_hint VARCHAR(500);

UPDATE guesthouse_translation
SET story_eyebrow = CASE language_code
        WHEN 'hu' THEN 'Csendes pihenés Csernakeresztúron'
        WHEN 'ro' THEN 'Odihnă liniștită în Cristur'
        ELSE 'A peaceful stay in Cristur'
    END,
    story_title = CASE language_code
        WHEN 'hu' THEN 'Egy kis falu, ahol megáll az idő'
        WHEN 'ro' THEN 'Un sat mic, unde timpul pare să se oprească'
        ELSE 'A small village where time stands still'
    END,
    dining_eyebrow = CASE language_code
        WHEN 'hu' THEN 'Házias erdélyi ízek'
        WHEN 'ro' THEN 'Gusturi ardelenești de casă'
        ELSE 'Homemade Transylvanian flavours'
    END,
    dining_title = CASE language_code
        WHEN 'hu' THEN 'Ételek, amelyek visszahívják vendégeinket'
        WHEN 'ro' THEN 'Mâncăruri care îi aduc pe oaspeți înapoi'
        ELSE 'Food that keeps our guests coming back'
    END,
    dining_description = CASE language_code
        WHEN 'hu' THEN 'Hagyományos, házi készítésű ételeink egyszerű, ismerős ízeket és valódi erdélyi vendéglátást hoznak az asztalra. Sok vendégünk évek óta ezekért az ízekért tér vissza hozzánk.'
        WHEN 'ro' THEN 'Preparatele noastre tradiționale, gătite în casă, aduc la masă gusturi familiare și ospitalitate ardelenească autentică. Mulți oaspeți se întorc de ani de zile pentru aceste arome.'
        ELSE 'Our traditional homemade dishes bring familiar flavours and genuine Transylvanian hospitality to the table. Many of our guests have returned for these tastes year after year.'
    END,
    amenities_title = CASE language_code
        WHEN 'hu' THEN 'Szolgáltatások'
        WHEN 'ro' THEN 'Servicii și facilități'
        ELSE 'Services and amenities'
    END,
    room_types_title = CASE language_code
        WHEN 'hu' THEN 'Szobatípusok'
        WHEN 'ro' THEN 'Tipuri de camere'
        ELSE 'Room types'
    END,
    pricing_title = CASE language_code
        WHEN 'hu' THEN 'Árak és feltételek'
        WHEN 'ro' THEN 'Prețuri și condiții'
        ELSE 'Prices and conditions'
    END,
    history_eyebrow = CASE language_code
        WHEN 'hu' THEN 'Történetünk és örökségünk'
        WHEN 'ro' THEN 'Povestea și moștenirea noastră'
        ELSE 'Our story and heritage'
    END,
    gallery_title = CASE language_code
        WHEN 'hu' THEN 'Képgaléria'
        WHEN 'ro' THEN 'Galerie foto'
        ELSE 'Gallery'
    END,
    gallery_hint = CASE language_code
        WHEN 'hu' THEN 'A nagyításhoz válasszon egy képet.'
        WHEN 'ro' THEN 'Selectați o imagine pentru a o mări.'
        ELSE 'Select an image to enlarge it.'
    END;

ALTER TABLE guesthouse_translation
    ALTER COLUMN story_eyebrow SET NOT NULL,
    ALTER COLUMN story_title SET NOT NULL,
    ALTER COLUMN dining_eyebrow SET NOT NULL,
    ALTER COLUMN dining_title SET NOT NULL,
    ALTER COLUMN dining_description SET NOT NULL,
    ALTER COLUMN amenities_title SET NOT NULL,
    ALTER COLUMN room_types_title SET NOT NULL,
    ALTER COLUMN pricing_title SET NOT NULL,
    ALTER COLUMN history_eyebrow SET NOT NULL,
    ALTER COLUMN gallery_title SET NOT NULL,
    ALTER COLUMN gallery_hint SET NOT NULL;
