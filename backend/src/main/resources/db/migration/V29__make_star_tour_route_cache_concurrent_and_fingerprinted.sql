ALTER TABLE star_tour_route_variant
    ADD COLUMN route_fingerprint VARCHAR(64),
    ADD COLUMN retry_after TIMESTAMPTZ;

UPDATE star_tour_route_variant
SET route_fingerprint = 'legacy-' || id::text;

ALTER TABLE star_tour_route_variant
    ALTER COLUMN route_fingerprint SET NOT NULL;

ALTER TABLE star_tour_route_variant
    DROP CONSTRAINT IF EXISTS star_tour_route_variant_star_tour_id_selection_key_key,
    DROP CONSTRAINT IF EXISTS star_tour_route_variant_calculation_status_check,
    DROP CONSTRAINT IF EXISTS star_tour_route_variant_check;

ALTER TABLE star_tour_route_variant
    ADD CONSTRAINT star_tour_route_variant_calculation_status_check
        CHECK (calculation_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    ADD CONSTRAINT star_tour_route_variant_state_check
        CHECK (
            (calculation_status = 'PENDING' AND failure_reason IS NULL AND retry_after IS NULL)
            OR (calculation_status = 'SUCCESS' AND failure_reason IS NULL AND retry_after IS NULL)
            OR (calculation_status = 'FAILED' AND failure_reason IS NOT NULL)
        ),
    ADD CONSTRAINT star_tour_route_variant_fingerprint_key
        UNIQUE (star_tour_id, route_fingerprint);

CREATE INDEX idx_star_tour_route_variant_retry_after
    ON star_tour_route_variant(calculation_status, retry_after);
