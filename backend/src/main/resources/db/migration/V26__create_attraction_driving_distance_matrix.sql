CREATE TABLE attraction_driving_distance (
    origin_attraction_id UUID NOT NULL REFERENCES attraction(id) ON DELETE CASCADE,
    destination_attraction_id UUID NOT NULL REFERENCES attraction(id) ON DELETE CASCADE,
    distance_meters INTEGER,
    duration_seconds INTEGER,
    calculation_status VARCHAR(16) NOT NULL CHECK (calculation_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    source VARCHAR(40) NOT NULL,
    calculated_at TIMESTAMPTZ,
    failure_reason VARCHAR(500),
    PRIMARY KEY (origin_attraction_id, destination_attraction_id),
    CHECK (origin_attraction_id <> destination_attraction_id),
    CHECK (
        (calculation_status = 'SUCCESS'
            AND distance_meters IS NOT NULL
            AND distance_meters >= 0
            AND duration_seconds IS NOT NULL
            AND duration_seconds >= 0
            AND failure_reason IS NULL)
        OR (calculation_status IN ('PENDING', 'FAILED')
            AND distance_meters IS NULL
            AND duration_seconds IS NULL)
    )
);

CREATE INDEX idx_attraction_driving_distance_status
    ON attraction_driving_distance(calculation_status);
