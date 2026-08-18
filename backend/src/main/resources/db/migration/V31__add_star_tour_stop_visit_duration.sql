ALTER TABLE star_tour_attraction
    ADD COLUMN planned_visit_duration_minutes INTEGER
        CHECK (planned_visit_duration_minutes BETWEEN 5 AND 720);
