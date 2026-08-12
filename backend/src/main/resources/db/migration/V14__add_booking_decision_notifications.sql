ALTER TABLE notification_outbox
    DROP CONSTRAINT notification_outbox_notification_type_check,
    DROP CONSTRAINT notification_outbox_check,
    ADD COLUMN guest_message TEXT;

ALTER TABLE notification_outbox
    ADD CONSTRAINT chk_notification_outbox_type CHECK (
        notification_type IN (
            'BOOKING_RECEIVED_GUEST',
            'BOOKING_RECEIVED_ADMIN',
            'BOOKING_CONFIRMED_GUEST',
            'BOOKING_REJECTED_GUEST'
        )
    ),
    ADD CONSTRAINT chk_notification_outbox_token CHECK (
        (notification_type = 'BOOKING_RECEIVED_GUEST'
         AND encrypted_management_token IS NOT NULL
         AND token_initialization_vector IS NOT NULL)
        OR notification_type IN (
            'BOOKING_RECEIVED_ADMIN',
            'BOOKING_CONFIRMED_GUEST',
            'BOOKING_REJECTED_GUEST'
        )
        OR status IN ('DELIVERED', 'EXHAUSTED')
    );
