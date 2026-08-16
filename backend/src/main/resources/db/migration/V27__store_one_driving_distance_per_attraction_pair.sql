CREATE TABLE attraction_driving_distance_pair (
    attraction_a_id UUID NOT NULL REFERENCES attraction(id) ON DELETE CASCADE,
    attraction_b_id UUID NOT NULL REFERENCES attraction(id) ON DELETE CASCADE,
    distance_meters INTEGER,
    duration_seconds INTEGER,
    calculation_status VARCHAR(16) NOT NULL CHECK (calculation_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    source VARCHAR(40) NOT NULL,
    calculated_at TIMESTAMPTZ,
    failure_reason VARCHAR(500),
    PRIMARY KEY (attraction_a_id, attraction_b_id),
    CHECK (attraction_a_id::text < attraction_b_id::text),
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

INSERT INTO attraction_driving_distance_pair (
    attraction_a_id,
    attraction_b_id,
    distance_meters,
    duration_seconds,
    calculation_status,
    source,
    calculated_at,
    failure_reason
)
SELECT DISTINCT ON (attraction_a_id, attraction_b_id)
    attraction_a_id,
    attraction_b_id,
    distance_meters,
    duration_seconds,
    calculation_status,
    source,
    calculated_at,
    failure_reason
FROM (
    SELECT
        CASE
            WHEN origin_attraction_id::text < destination_attraction_id::text THEN origin_attraction_id
            ELSE destination_attraction_id
        END AS attraction_a_id,
        CASE
            WHEN origin_attraction_id::text < destination_attraction_id::text THEN destination_attraction_id
            ELSE origin_attraction_id
        END AS attraction_b_id,
        distance_meters,
        duration_seconds,
        calculation_status,
        source,
        calculated_at,
        failure_reason
    FROM attraction_driving_distance
) AS directional_distances
ORDER BY attraction_a_id, attraction_b_id, calculated_at DESC NULLS LAST;

DROP TABLE attraction_driving_distance;
ALTER TABLE attraction_driving_distance_pair RENAME TO attraction_driving_distance;
CREATE INDEX idx_attraction_driving_distance_status
    ON attraction_driving_distance(calculation_status);
