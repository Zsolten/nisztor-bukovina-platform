ALTER TABLE star_tour_attraction
    ADD COLUMN optional_stop BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO attraction (id, slug, latitude, longitude, google_maps_url, active)
VALUES (
    '28c2ed0e-588f-514c-ab1d-98a2c4ba5d03',
    'csernakereszturi-tajhaz',
    45.825720,
    22.943124,
    'https://www.google.com/maps/search/?api=1&query=Csernakereszt%C3%BAri%20T%C3%A1jh%C3%A1z%2C%20Cristur%2C%20Romania',
    TRUE
);

INSERT INTO attraction_translation (
    attraction_id,
    language_code,
    name,
    short_description,
    detailed_description,
    admission_information,
    practical_information
)
VALUES (
    '28c2ed0e-588f-514c-ab1d-98a2c4ba5d03',
    'hu',
    'Csernakeresztúri tájház',
    'Bukovinai székely emléktárgyakat és a helyi közösség hagyományait bemutató falumúzeum.',
    'A Csernakeresztúri tájház a bukovinai székely közösség tárgyi emlékeit őrzi. A gyűjteményt a helyi hagyományőrző egyesület tagjai állították össze a Bukovinából hozott és a környéken megőrzött használati tárgyakból.',
    'Ingyenes.',
    NULL
);

INSERT INTO attraction_collection (attraction_id, collection_id, display_order)
SELECT
    '28c2ed0e-588f-514c-ab1d-98a2c4ba5d03',
    id,
    100
FROM tourism_collection
WHERE slug = 'maros-mente';

INSERT INTO star_tour (id, slug, map_color, published, active)
VALUES
    ('1cb58299-6d38-5e68-a571-594b1c6bd5dd', 'paring-es-hatszegi-medence', '#C65D3B', TRUE, TRUE),
    ('4605e0e9-e5d6-5082-a0fc-c0ec24e16802', 'maros-mente-es-gyulafehervar', '#376C8A', TRUE, TRUE);

INSERT INTO star_tour_translation (
    star_tour_id,
    language_code,
    name,
    short_description,
    detailed_description
)
VALUES
    (
        '1cb58299-6d38-5e68-a571-594b1c6bd5dd',
        'hu',
        'Páring és a Hátszegi-medence',
        'Egynapos csillagtúra hegyvidéki, természeti és történelmi látnivalókkal.',
        'A túra a Páring-hegység panorámájától a Véka-szurdokon és a Bóli-barlangon át a Vajdahunyadi kastélyig vezet. Ha az idő engedi, az Őraljaboldogfalvi református templom és a Demsusi kőtemplom is beilleszthető a programba.'
    ),
    (
        '4605e0e9-e5d6-5082-a0fc-c0ec24e16802',
        'hu',
        'Maros mente és Gyulafehérvár',
        'Egynapos történelmi és kulturális körút Dévától Gyulafehérvárig.',
        'A csillagtúra Déva várát, Algyógyfürdő római emlékeit és vízesését, Gyulafehérvár erődjét és székesegyházait, valamint a Csernakeresztúri tájház bukovinai székely örökségét kapcsolja össze.'
    );

INSERT INTO star_tour_tag (star_tour_id, tag)
VALUES
    ('1cb58299-6d38-5e68-a571-594b1c6bd5dd', 'egynapos'),
    ('1cb58299-6d38-5e68-a571-594b1c6bd5dd', 'örökség'),
    ('1cb58299-6d38-5e68-a571-594b1c6bd5dd', 'természet'),
    ('4605e0e9-e5d6-5082-a0fc-c0ec24e16802', 'egynapos'),
    ('4605e0e9-e5d6-5082-a0fc-c0ec24e16802', 'örökség'),
    ('4605e0e9-e5d6-5082-a0fc-c0ec24e16802', 'városnézés');

INSERT INTO star_tour_attraction (star_tour_id, attraction_id, display_order, optional_stop)
SELECT '1cb58299-6d38-5e68-a571-594b1c6bd5dd'::uuid, id, 0, FALSE FROM attraction WHERE slug = 'paring-hegyseg'
UNION ALL
SELECT '1cb58299-6d38-5e68-a571-594b1c6bd5dd', id, 1, FALSE FROM attraction WHERE slug = 'veka-szurdok'
UNION ALL
SELECT '1cb58299-6d38-5e68-a571-594b1c6bd5dd', id, 2, FALSE FROM attraction WHERE slug = 'boli-barlang'
UNION ALL
SELECT '1cb58299-6d38-5e68-a571-594b1c6bd5dd', id, 3, FALSE FROM attraction WHERE slug = 'vajdahunyadi-kastely'
UNION ALL
SELECT '1cb58299-6d38-5e68-a571-594b1c6bd5dd', id, 4, TRUE FROM attraction WHERE slug = 'oraljaboldogfalvi-reformatus-templom'
UNION ALL
SELECT '1cb58299-6d38-5e68-a571-594b1c6bd5dd', id, 5, TRUE FROM attraction WHERE slug = 'demsusi-kotemplom';

INSERT INTO star_tour_attraction (star_tour_id, attraction_id, display_order, optional_stop)
SELECT '4605e0e9-e5d6-5082-a0fc-c0ec24e16802'::uuid, id, 0, FALSE FROM attraction WHERE slug = 'deva-vara'
UNION ALL
SELECT '4605e0e9-e5d6-5082-a0fc-c0ec24e16802', id, 1, FALSE FROM attraction WHERE slug = 'algyogyfurdo'
UNION ALL
SELECT '4605e0e9-e5d6-5082-a0fc-c0ec24e16802', id, 2, FALSE FROM attraction WHERE slug = 'gyulafehervar'
UNION ALL
SELECT '4605e0e9-e5d6-5082-a0fc-c0ec24e16802', id, 3, FALSE FROM attraction WHERE slug = 'csernakereszturi-tajhaz';
