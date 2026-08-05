DELETE FROM guesthouse_image image
USING guesthouse
WHERE image.guesthouse_id = guesthouse.id
  AND (
    (
      guesthouse.slug = 'nisztor-panzio'
      AND image.path IN (
        '/images/guesthouses/nisztor/gallery-11.jpg',
        '/images/guesthouses/nisztor/gallery-12.jpg',
        '/images/guesthouses/nisztor/gallery-13.jpg',
        '/images/guesthouses/nisztor/gallery-14.jpg',
        '/images/guesthouses/nisztor/gallery-15.jpg',
        '/images/guesthouses/nisztor/gallery-16.jpg',
        '/images/guesthouses/nisztor/gallery-17.jpg',
        '/images/guesthouses/nisztor/gallery-18.jpg',
        '/images/guesthouses/nisztor/gallery-19.jpg',
        '/images/guesthouses/nisztor/gallery-20.jpg',
        '/images/guesthouses/nisztor/gallery-21.jpg',
        '/images/guesthouses/nisztor/gallery-22.jpg',
        '/images/guesthouses/nisztor/gallery-23.jpg',
        '/images/guesthouses/nisztor/gallery-24.jpg',
        '/images/guesthouses/nisztor/gallery-25.jpg',
        '/images/guesthouses/nisztor/gallery-26.jpg'
      )
    )
    OR
    (
      guesthouse.slug = 'bukovina-panzio'
      AND image.path IN (
        '/images/guesthouses/bukovina/gallery-10.jpg',
        '/images/guesthouses/bukovina/gallery-11.jpg',
        '/images/guesthouses/bukovina/gallery-12.jpg',
        '/images/guesthouses/bukovina/gallery-14.jpg',
        '/images/guesthouses/bukovina/gallery-18.jpg',
        '/images/guesthouses/bukovina/gallery-19.jpg',
        '/images/guesthouses/bukovina/gallery-20.jpg',
        '/images/guesthouses/bukovina/gallery-21.jpg',
        '/images/guesthouses/bukovina/gallery-23.jpg',
        '/images/guesthouses/bukovina/gallery-24.jpg',
        '/images/guesthouses/bukovina/gallery-25.jpg',
        '/images/guesthouses/bukovina/gallery-26.jpg',
        '/images/guesthouses/bukovina/gallery-29.jpg',
        '/images/guesthouses/bukovina/gallery-30.jpg',
        '/images/guesthouses/bukovina/gallery-31.jpg',
        '/images/guesthouses/bukovina/gallery-32.jpg',
        '/images/guesthouses/bukovina/gallery-33.jpg',
        '/images/guesthouses/bukovina/gallery-34.jpg'
      )
    )
  );

CREATE TEMP TABLE refreshed_guesthouse_image_alt (
  path VARCHAR(500) PRIMARY KEY,
  hu VARCHAR(300) NOT NULL,
  ro VARCHAR(300) NOT NULL,
  en VARCHAR(300) NOT NULL
) ON COMMIT DROP;

INSERT INTO refreshed_guesthouse_image_alt (path, hu, ro, en) VALUES
  (
    '/images/guesthouses/nisztor/gallery-01.jpg',
    'A Nisztor Panzió utcai homlokzata',
    'Fațada stradală a Pensiunii Nisztor',
    'Street-facing facade of Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/nisztor/gallery-02.jpg',
    'Franciaágyas szoba sötét kárpitozott ággyal a Nisztor Panzióban',
    'Cameră dublă cu pat tapițat închis la culoare la Pensiunea Nisztor',
    'Double room with a dark upholstered bed at Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/nisztor/gallery-03.jpg',
    'A Nisztor Panzió udvara a díszített kerítésen keresztül',
    'Curtea Pensiunii Nisztor văzută prin gardul decorativ',
    'Nisztor Guesthouse courtyard seen through the decorative fence'
  ),
  (
    '/images/guesthouses/nisztor/gallery-04.jpg',
    'Háromágyas szoba faburkolatú fallal a Nisztor Panzióban',
    'Cameră triplă cu perete placat cu lemn la Pensiunea Nisztor',
    'Triple room with a wood-panelled wall at Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/nisztor/gallery-05.jpg',
    'Rózsaszín virágok és kerámia tyúk asztali díszként',
    'Flori roz și o găină din ceramică folosite ca decor de masă',
    'Pink flowers and a ceramic hen used as table decoration'
  ),
  (
    '/images/guesthouses/nisztor/gallery-06.jpg',
    'Háromágyas szoba kanapéval és televízióval a Nisztor Panzióban',
    'Cameră triplă cu canapea și televizor la Pensiunea Nisztor',
    'Triple room with a sofa and television at Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/nisztor/gallery-07.jpg',
    'Két különálló fehér ágyas szoba a Nisztor Panzióban',
    'Cameră cu două paturi albe separate la Pensiunea Nisztor',
    'Room with two separate white beds at Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/nisztor/gallery-08.jpg',
    'Szobabejárat falépcsővel és csomagtartó paddal a Nisztor Panzióban',
    'Intrare în cameră cu scară din lemn și banchetă pentru bagaje la Pensiunea Nisztor',
    'Room entrance with wooden stairs and a luggage bench at Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/nisztor/gallery-09.jpg',
    'Egyszemélyes ágy és a galériára vezető falépcső a Nisztor Panzióban',
    'Pat de o persoană și scară din lemn spre mansardă la Pensiunea Nisztor',
    'Single bed and wooden stairs leading to the loft at Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/nisztor/gallery-10.jpg',
    'Kétágyas galériaszoba légkondicionálóval a Nisztor Panzióban',
    'Cameră mansardată cu două paturi și aer condiționat la Pensiunea Nisztor',
    'Twin loft room with air conditioning at Nisztor Guesthouse'
  ),
  (
    '/images/guesthouses/bukovina/gallery-01.jpg',
    'Kétágyas szoba napraforgós festménnyel a Bukovina Panzióban',
    'Cameră dublă cu tablou cu floarea-soarelui la Pensiunea Bukovina',
    'Double room with a sunflower painting at Bukovina Guesthouse'
  ),
  (
    '/images/guesthouses/bukovina/gallery-02.jpg',
    'Kanapé tetőablak alatt a Bukovina Panzió tetőterében',
    'Canapea sub fereastra de mansardă la Pensiunea Bukovina',
    'Sofa beneath a skylight in the attic of Bukovina Guesthouse'
  ),
  (
    '/images/guesthouses/bukovina/gallery-03.jpg',
    'Faragott Szűz Mária és gyermek szobor a Bukovina Panzióban',
    'Statuie sculptată a Fecioarei Maria cu Pruncul la Pensiunea Bukovina',
    'Carved statue of the Virgin Mary and Child at Bukovina Guesthouse'
  ),
  (
    '/images/guesthouses/bukovina/gallery-04.jpg',
    'Franciaágyas szoba fabútorokkal a Bukovina Panzióban',
    'Cameră dublă cu mobilier din lemn la Pensiunea Bukovina',
    'Double room with wooden furniture at Bukovina Guesthouse'
  ),
  (
    '/images/guesthouses/bukovina/gallery-05.jpg',
    'Hagyományos dísztárgyak és régi telefonok a Bukovina Panzió recepcióján',
    'Decorațiuni tradiționale și telefoane vechi la recepția Pensiunii Bukovina',
    'Traditional decorations and vintage telephones at the Bukovina Guesthouse reception'
  ),
  (
    '/images/guesthouses/bukovina/gallery-06.jpg',
    'Piros virágok és fa madárház a Bukovina Panzió kertjében',
    'Flori roșii și căsuță din lemn pentru păsări în grădina Pensiunii Bukovina',
    'Red flowers and a wooden birdhouse in the Bukovina Guesthouse garden'
  ),
  (
    '/images/guesthouses/bukovina/gallery-08.jpg',
    'A Bukovina Panzió udvari homlokzata a díszített kerítésen keresztül',
    'Fațada din curte a Pensiunii Bukovina văzută prin gardul decorativ',
    'Courtyard facade of Bukovina Guesthouse seen through the decorative fence'
  );

UPDATE guesthouse_image image
SET alt_text = image_alt.hu
FROM refreshed_guesthouse_image_alt image_alt
WHERE image.path = image_alt.path;

UPDATE guesthouse_image_translation translation
SET alt_text = CASE translation.language_code
    WHEN 'hu' THEN image_alt.hu
    WHEN 'ro' THEN image_alt.ro
    WHEN 'en' THEN image_alt.en
    ELSE translation.alt_text
  END
FROM guesthouse_image image
JOIN refreshed_guesthouse_image_alt image_alt ON image_alt.path = image.path
WHERE translation.image_id = image.id;
