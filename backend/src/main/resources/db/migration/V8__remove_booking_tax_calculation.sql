DELETE FROM tax_configuration
WHERE code = 'accommodation_tax';

UPDATE tax_configuration
SET display_order = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'city_tax';

UPDATE booking_request
SET total_payable = net_accommodation
    + single_room_surcharge
    + breakfast_total
    + dinner_total;

ALTER TABLE booking_request
    RENAME COLUMN net_accommodation TO accommodation_total;

ALTER TABLE booking_request
    DROP COLUMN accommodation_tax_rate,
    DROP COLUMN accommodation_tax_amount,
    DROP COLUMN city_tax_rate,
    DROP COLUMN city_tax_amount;
