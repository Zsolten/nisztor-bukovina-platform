CREATE TABLE star_tour_route_variant (
    id UUID PRIMARY KEY,
    star_tour_id UUID NOT NULL REFERENCES star_tour(id) ON DELETE CASCADE,
    selection_key VARCHAR(500) NOT NULL,
    calculation_status VARCHAR(16) NOT NULL CHECK (calculation_status IN ('SUCCESS', 'FAILED')),
    source VARCHAR(40) NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL,
    failure_reason VARCHAR(500),
    UNIQUE (star_tour_id, selection_key),
    CHECK (
        (calculation_status = 'SUCCESS' AND failure_reason IS NULL)
        OR (calculation_status = 'FAILED' AND failure_reason IS NOT NULL)
    )
);

CREATE TABLE star_tour_route_leg (
    route_variant_id UUID NOT NULL REFERENCES star_tour_route_variant(id) ON DELETE CASCADE,
    leg_order INTEGER NOT NULL CHECK (leg_order >= 0),
    from_stop_index INTEGER NOT NULL CHECK (from_stop_index >= 0),
    to_stop_index INTEGER NOT NULL CHECK (to_stop_index = from_stop_index + 1),
    distance_meters INTEGER NOT NULL CHECK (distance_meters >= 0),
    duration_seconds INTEGER NOT NULL CHECK (duration_seconds >= 0),
    encoded_polyline TEXT NOT NULL CHECK (length(encoded_polyline) > 0),
    PRIMARY KEY (route_variant_id, leg_order),
    UNIQUE (route_variant_id, from_stop_index),
    UNIQUE (route_variant_id, to_stop_index)
);

CREATE INDEX idx_star_tour_route_variant_tour_status
    ON star_tour_route_variant(star_tour_id, calculation_status);
