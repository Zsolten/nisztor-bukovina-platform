CREATE TABLE guesthouse_notification_recipient (
    id UUID PRIMARY KEY,
    guesthouse_id UUID NOT NULL REFERENCES guesthouse (id) ON DELETE CASCADE,
    email VARCHAR(320) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_guesthouse_notification_recipient_normalized
    ON guesthouse_notification_recipient (guesthouse_id, LOWER(email));

CREATE INDEX idx_guesthouse_notification_recipient_active
    ON guesthouse_notification_recipient (guesthouse_id, active);

CREATE TABLE notification_outbox (
    id UUID PRIMARY KEY,
    booking_request_id UUID NOT NULL REFERENCES booking_request (id) ON DELETE CASCADE,
    notification_type VARCHAR(40) NOT NULL CHECK (
        notification_type IN ('BOOKING_RECEIVED_GUEST', 'BOOKING_RECEIVED_ADMIN')
    ),
    recipient VARCHAR(320) NOT NULL,
    reply_to VARCHAR(320),
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    status VARCHAR(20) NOT NULL CHECK (
        status IN ('PENDING', 'PROCESSING', 'RETRY', 'DELIVERED', 'EXHAUSTED')
    ),
    attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    last_error_code VARCHAR(100),
    encrypted_management_token TEXT,
    token_initialization_vector VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (booking_request_id, notification_type, recipient),
    CHECK (
        (notification_type = 'BOOKING_RECEIVED_GUEST'
         AND encrypted_management_token IS NOT NULL
         AND token_initialization_vector IS NOT NULL)
        OR notification_type = 'BOOKING_RECEIVED_ADMIN'
        OR status IN ('DELIVERED', 'EXHAUSTED')
    )
);

CREATE INDEX idx_notification_outbox_pending
    ON notification_outbox (status, next_attempt_at, created_at);
