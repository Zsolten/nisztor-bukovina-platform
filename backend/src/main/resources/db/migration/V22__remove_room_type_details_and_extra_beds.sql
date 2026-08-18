ALTER TABLE room_type_translation
    DROP COLUMN detailed_description;

UPDATE room_type
SET rooms_with_extra_bed = 0,
    extra_beds_per_eligible_room = 0;
