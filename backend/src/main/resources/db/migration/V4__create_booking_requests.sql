CREATE TABLE tax_configuration (
    code VARCHAR(80) PRIMARY KEY,
    percentage NUMERIC(5, 2) NOT NULL CHECK (percentage BETWEEN 0 AND 100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tax_configuration_translation (
    tax_code VARCHAR(80) NOT NULL REFERENCES tax_configuration (code) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    label VARCHAR(240) NOT NULL,
    PRIMARY KEY (tax_code, language_code)
);

INSERT INTO tax_configuration (code, percentage, active, display_order)
VALUES ('accommodation_tax', 11.00, TRUE, 0),
       ('city_tax', 1.00, TRUE, 1);

INSERT INTO tax_configuration_translation (tax_code, language_code, label)
VALUES ('accommodation_tax', 'hu', 'Szállás áfája'),
       ('accommodation_tax', 'ro', 'TVA pentru cazare'),
       ('accommodation_tax', 'en', 'Accommodation VAT'),
       ('city_tax', 'hu', 'Idegenforgalmi adó'),
       ('city_tax', 'ro', 'Taxă turistică'),
       ('city_tax', 'en', 'City tax');

DELETE FROM pricing_adjustment
WHERE code = 'tourist_tax';

CREATE TABLE booking_request (
    id UUID PRIMARY KEY,
    guesthouse_id UUID NOT NULL REFERENCES guesthouse (id),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    adults INTEGER NOT NULL CHECK (adults >= 1),
    children_age_3_to_10 INTEGER NOT NULL CHECK (children_age_3_to_10 >= 0),
    children_age_0_to_3 INTEGER NOT NULL CHECK (children_age_0_to_3 >= 0),
    contact_name VARCHAR(160) NOT NULL,
    contact_email VARCHAR(320) NOT NULL,
    contact_phone VARCHAR(40) NOT NULL,
    preferred_language VARCHAR(2) NOT NULL CHECK (preferred_language IN ('hu', 'ro', 'en')),
    note TEXT,
    status VARCHAR(32) NOT NULL CHECK (
        status IN ('RECEIVED', 'UNDER_REVIEW', 'CONFIRMED', 'REJECTED', 'CANCELLED')
    ),
    net_accommodation NUMERIC(12, 2) NOT NULL CHECK (net_accommodation >= 0),
    accommodation_tax_rate NUMERIC(5, 2) NOT NULL CHECK (accommodation_tax_rate BETWEEN 0 AND 100),
    accommodation_tax_amount NUMERIC(12, 2) NOT NULL CHECK (accommodation_tax_amount >= 0),
    single_room_surcharge NUMERIC(12, 2) NOT NULL CHECK (single_room_surcharge >= 0),
    city_tax_rate NUMERIC(5, 2) NOT NULL CHECK (city_tax_rate BETWEEN 0 AND 100),
    city_tax_amount NUMERIC(12, 2) NOT NULL CHECK (city_tax_amount >= 0),
    total_payable NUMERIC(12, 2) NOT NULL CHECK (total_payable >= 0),
    management_token_hash VARCHAR(128) NOT NULL UNIQUE,
    management_token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (check_out_date > check_in_date)
);

CREATE INDEX idx_booking_request_guesthouse_id ON booking_request (guesthouse_id);
CREATE INDEX idx_booking_request_status ON booking_request (status);
CREATE INDEX idx_booking_request_contact_email ON booking_request (contact_email);
CREATE INDEX idx_booking_request_created_at ON booking_request (created_at);

CREATE TABLE booking_room_selection (
    id UUID PRIMARY KEY,
    booking_request_id UUID NOT NULL REFERENCES booking_request (id) ON DELETE CASCADE,
    room_type_id UUID NOT NULL REFERENCES room_type (id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    UNIQUE (booking_request_id, room_type_id)
);

CREATE INDEX idx_booking_room_selection_booking_request_id
    ON booking_room_selection (booking_request_id);
CREATE INDEX idx_booking_room_selection_room_type_id
    ON booking_room_selection (room_type_id);

CREATE TABLE booking_status_history (
    id UUID PRIMARY KEY,
    booking_request_id UUID NOT NULL REFERENCES booking_request (id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL CHECK (
        status IN ('RECEIVED', 'UNDER_REVIEW', 'CONFIRMED', 'REJECTED', 'CANCELLED')
    ),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    changed_by VARCHAR(160) NOT NULL
);

CREATE INDEX idx_booking_status_history_booking_request_id
    ON booking_status_history (booking_request_id);
CREATE INDEX idx_booking_status_history_changed_at
    ON booking_status_history (changed_at);
