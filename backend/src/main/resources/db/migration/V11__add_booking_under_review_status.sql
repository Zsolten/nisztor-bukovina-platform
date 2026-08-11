ALTER TABLE booking_request
    DROP CONSTRAINT booking_request_status_check;

ALTER TABLE booking_status_history
    DROP CONSTRAINT booking_status_history_status_check;

ALTER TABLE booking_request
    ADD CONSTRAINT booking_request_status_check CHECK (
        status IN ('RECEIVED', 'UNDER_REVIEW', 'CONFIRMED', 'REJECTED', 'CANCELLED')
    );

ALTER TABLE booking_status_history
    ADD CONSTRAINT booking_status_history_status_check CHECK (
        status IN ('RECEIVED', 'UNDER_REVIEW', 'CONFIRMED', 'REJECTED', 'CANCELLED')
    );
