CREATE TABLE tourism_collection (
    id UUID PRIMARY KEY,
    slug VARCHAR(120) NOT NULL UNIQUE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tourism_collection_translation (
    collection_id UUID NOT NULL REFERENCES tourism_collection(id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    name VARCHAR(180) NOT NULL,
    short_description TEXT,
    PRIMARY KEY (collection_id, language_code)
);

CREATE TABLE attraction (
    id UUID PRIMARY KEY,
    slug VARCHAR(160) NOT NULL UNIQUE,
    latitude NUMERIC(9, 6) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude NUMERIC(9, 6) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    google_maps_url TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attraction_translation (
    attraction_id UUID NOT NULL REFERENCES attraction(id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    name VARCHAR(220) NOT NULL,
    short_description TEXT NOT NULL,
    detailed_description TEXT NOT NULL,
    admission_information TEXT,
    practical_information TEXT,
    PRIMARY KEY (attraction_id, language_code)
);

CREATE TABLE attraction_collection (
    attraction_id UUID NOT NULL REFERENCES attraction(id) ON DELETE CASCADE,
    collection_id UUID NOT NULL REFERENCES tourism_collection(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    PRIMARY KEY (attraction_id, collection_id),
    UNIQUE (collection_id, display_order)
);

CREATE TABLE star_tour (
    id UUID PRIMARY KEY,
    slug VARCHAR(160) NOT NULL UNIQUE,
    map_color VARCHAR(7) NOT NULL CHECK (map_color ~ '^#[0-9A-Fa-f]{6}$'),
    published BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE star_tour_translation (
    star_tour_id UUID NOT NULL REFERENCES star_tour(id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    name VARCHAR(220) NOT NULL,
    short_description TEXT NOT NULL,
    detailed_description TEXT NOT NULL,
    PRIMARY KEY (star_tour_id, language_code)
);

CREATE TABLE star_tour_image (
    id UUID PRIMARY KEY,
    star_tour_id UUID NOT NULL REFERENCES star_tour(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    UNIQUE (star_tour_id, display_order)
);

CREATE TABLE star_tour_image_translation (
    image_id UUID NOT NULL REFERENCES star_tour_image(id) ON DELETE CASCADE,
    language_code VARCHAR(2) NOT NULL CHECK (language_code IN ('hu', 'ro', 'en')),
    alt_text VARCHAR(300) NOT NULL,
    PRIMARY KEY (image_id, language_code)
);

CREATE TABLE star_tour_tag (
    star_tour_id UUID NOT NULL REFERENCES star_tour(id) ON DELETE CASCADE,
    tag VARCHAR(80) NOT NULL,
    PRIMARY KEY (star_tour_id, tag)
);

-- The ordered stop editor belongs to TOUR-007, but the relation is established here so
-- attractions never depend on a single tour and can participate in several routes.
CREATE TABLE star_tour_attraction (
    star_tour_id UUID NOT NULL REFERENCES star_tour(id) ON DELETE CASCADE,
    attraction_id UUID NOT NULL REFERENCES attraction(id) ON DELETE RESTRICT,
    display_order INTEGER NOT NULL CHECK (display_order >= 0),
    PRIMARY KEY (star_tour_id, attraction_id),
    UNIQUE (star_tour_id, display_order)
);

CREATE INDEX idx_attraction_active ON attraction(active);
CREATE INDEX idx_star_tour_public ON star_tour(published, active);
CREATE INDEX idx_attraction_collection_collection ON attraction_collection(collection_id);
