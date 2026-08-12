ALTER TABLE booking_request
    ADD COLUMN adult_accommodation_total NUMERIC(12, 2),
    ADD COLUMN child_accommodation_total NUMERIC(12, 2);

UPDATE booking_request
SET adult_accommodation_total = ROUND(
        accommodation_total * adults
        / (adults + children_age_3_to_10 * 0.75),
        2
    );

UPDATE booking_request
SET child_accommodation_total = accommodation_total - adult_accommodation_total;

ALTER TABLE booking_request
    ALTER COLUMN adult_accommodation_total SET NOT NULL,
    ALTER COLUMN child_accommodation_total SET NOT NULL,
    ADD CONSTRAINT chk_booking_adult_accommodation_total
        CHECK (adult_accommodation_total >= 0),
    ADD CONSTRAINT chk_booking_child_accommodation_total
        CHECK (child_accommodation_total >= 0);
