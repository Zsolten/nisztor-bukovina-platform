ALTER TABLE booking_request
    DROP CONSTRAINT booking_request_status_check;

ALTER TABLE booking_status_history
    DROP CONSTRAINT booking_status_history_status_check;

ALTER TABLE booking_request
    ADD CONSTRAINT booking_request_status_check CHECK (
        status IN ('RECEIVED', 'CONFIRMED', 'REJECTED', 'CANCELLED')
    );

ALTER TABLE booking_status_history
    ADD CONSTRAINT booking_status_history_status_check CHECK (
        status IN ('RECEIVED', 'CONFIRMED', 'REJECTED', 'CANCELLED')
    );

ALTER TABLE booking_request
    ADD COLUMN public_reference VARCHAR(24),
    ADD COLUMN idempotency_key_hash VARCHAR(64),
    ADD COLUMN request_fingerprint VARCHAR(64),
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'RON' CHECK (currency = 'RON'),
    ADD COLUMN breakfast_participants INTEGER NOT NULL DEFAULT 0 CHECK (breakfast_participants >= 0),
    ADD COLUMN dinner_participants INTEGER NOT NULL DEFAULT 0 CHECK (dinner_participants >= 0),
    ADD COLUMN breakfast_total NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (breakfast_total >= 0),
    ADD COLUMN dinner_total NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (dinner_total >= 0);

UPDATE booking_request
SET public_reference = 'NB-' || UPPER(SUBSTRING(REPLACE(id::TEXT, '-', '') FROM 1 FOR 12)),
    idempotency_key_hash = MD5(id::TEXT || ':idempotency') || MD5(id::TEXT || ':idempotency:2'),
    request_fingerprint = MD5(id::TEXT || ':request') || MD5(id::TEXT || ':request:2')
WHERE public_reference IS NULL;

ALTER TABLE booking_request
    ALTER COLUMN public_reference SET NOT NULL,
    ALTER COLUMN idempotency_key_hash SET NOT NULL,
    ALTER COLUMN request_fingerprint SET NOT NULL;

CREATE UNIQUE INDEX uq_booking_request_public_reference
    ON booking_request (public_reference);

CREATE UNIQUE INDEX uq_booking_request_idempotency_key_hash
    ON booking_request (idempotency_key_hash);
