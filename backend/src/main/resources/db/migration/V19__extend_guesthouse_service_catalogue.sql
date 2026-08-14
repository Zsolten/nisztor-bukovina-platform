ALTER TABLE amenity
    ADD COLUMN pricing_type VARCHAR(10) NOT NULL DEFAULT 'FREE'
        CHECK (pricing_type IN ('FREE', 'PAID'));

ALTER TABLE amenity_translation
    ADD COLUMN detailed_description TEXT;
