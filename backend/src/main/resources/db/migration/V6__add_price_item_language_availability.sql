CREATE TABLE price_item_language_availability (
    price_item_id UUID NOT NULL REFERENCES price_item (id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    PRIMARY KEY (price_item_id, language_code)
);

INSERT INTO price_item_language_availability (price_item_id, language_code)
SELECT item.id, language.language_code
FROM price_item item
CROSS JOIN (VALUES ('hu'), ('ro'), ('en')) AS language(language_code)
WHERE item.code <> 'tour_guide';

INSERT INTO price_item_language_availability (price_item_id, language_code)
SELECT id, 'hu'
FROM price_item
WHERE code = 'tour_guide';
