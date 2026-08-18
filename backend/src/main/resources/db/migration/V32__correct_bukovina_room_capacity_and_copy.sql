-- Owner-confirmed Bukovina room capacity and public room information.
UPDATE guesthouse
SET room_count = 20
WHERE slug = 'bukovina-panzio';

UPDATE guesthouse_translation translation
SET short_description = CASE translation.language_code
        WHEN 'hu' THEN 'Családias szállás 20 kényelmes szobával Csernakeresztúron.'
        WHEN 'ro' THEN 'Cazare familială în Cristur, cu 20 de camere confortabile.'
        ELSE 'Family-friendly accommodation in Cristur with 20 comfortable rooms.'
    END,
    description = CASE translation.language_code
        WHEN 'hu' THEN 'A Bukovina Panzió 20 kényelmes szobával várja vendégeit Csernakeresztúron. Egy-, két-, három- és négyágyas szobák közül lehet választani az igényekhez igazodva. Minden szoba saját fürdőszobával, fűtéssel és légkondicionálással rendelkezik.'
        WHEN 'ro' THEN 'Pensiunea Bukovina oferă 20 de camere confortabile în Cristur. Oaspeții pot alege camere single, duble, triple sau cvadruple, în funcție de nevoi. Fiecare cameră are baie proprie, încălzire și aer condiționat.'
        ELSE 'Bukovina Guesthouse offers 20 comfortable rooms in Cristur. Guests can choose single, double, triple or quadruple rooms according to their needs. Every room has a private bathroom, heating and air conditioning.'
    END,
    room_description = CASE translation.language_code
        WHEN 'hu' THEN '20 szoba egy-, két-, három- és négyágyas elrendezésben, az igényekhez igazodva.'
        WHEN 'ro' THEN '20 de camere în configurații single, duble, triple și cvadruple, adaptate nevoilor oaspeților.'
        ELSE '20 rooms in single, double, triple and quadruple configurations to suit guests'' needs.'
    END
FROM guesthouse
WHERE translation.guesthouse_id = guesthouse.id
  AND guesthouse.slug = 'bukovina-panzio';
