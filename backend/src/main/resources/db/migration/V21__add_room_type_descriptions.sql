ALTER TABLE room_type_translation
    ADD COLUMN short_description VARCHAR(1000),
    ADD COLUMN detailed_description TEXT;

UPDATE room_type_translation
SET short_description = CASE language_code
    WHEN 'hu' THEN 'Kényelmes szobatípus a panzióban.'
    WHEN 'ro' THEN 'Tip de cameră confortabil în pensiune.'
    ELSE 'A comfortable room type at the guesthouse.'
END
WHERE short_description IS NULL;

ALTER TABLE room_type_translation
    ALTER COLUMN short_description SET NOT NULL;
