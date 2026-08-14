-- Room selections are preference requests, not a live physical-inventory system.
-- A type may therefore be selected up to fifteen times even where the underlying rooms overlap.
UPDATE room_type
SET quantity = 15,
    display_order = display_order + 1;

INSERT INTO room_type (
    guesthouse_id, code, quantity, standard_occupancy, rooms_with_extra_bed,
    extra_beds_per_eligible_room, active, display_order
)
SELECT id, 'single', 15, 1, 0, 0, TRUE, 0
FROM guesthouse;

INSERT INTO room_type_translation (room_type_id, language_code, name)
SELECT room_type.id,
       language.language_code,
       CASE language.language_code
         WHEN 'hu' THEN 'Egyágyas szoba'
         WHEN 'ro' THEN 'Cameră single'
         ELSE 'Single room'
       END
FROM room_type
CROSS JOIN (VALUES ('hu'), ('ro'), ('en')) AS language(language_code)
WHERE room_type.code = 'single';

INSERT INTO room_type_feature (room_type_id, amenity_id, display_order)
SELECT single_room.id, feature.amenity_id, feature.display_order
FROM room_type single_room
JOIN room_type double_room
  ON double_room.guesthouse_id = single_room.guesthouse_id
 AND double_room.code = 'double'
JOIN room_type_feature feature ON feature.room_type_id = double_room.id
WHERE single_room.code = 'single';

ALTER TABLE price_item DROP CONSTRAINT IF EXISTS price_item_unit_check;
ALTER TABLE price_item
    ADD CONSTRAINT price_item_unit_check
    CHECK (unit IN ('person_night', 'room_night', 'person', 'day'));

UPDATE price_item
SET code = 'single_room',
    unit = 'room_night'
WHERE code = 'single_occupancy_room';

UPDATE price_item_translation translation
SET label = CASE translation.language_code
    WHEN 'hu' THEN 'Egyágyas szoba'
    WHEN 'ro' THEN 'Cameră single'
    ELSE 'Single room'
END
FROM price_item item
WHERE translation.price_item_id = item.id
  AND item.code = 'single_room';
