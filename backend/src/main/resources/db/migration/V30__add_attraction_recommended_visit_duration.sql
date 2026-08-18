ALTER TABLE attraction
    ADD COLUMN recommended_visit_duration_minutes INTEGER NOT NULL DEFAULT 60
        CHECK (recommended_visit_duration_minutes BETWEEN 5 AND 720);

UPDATE attraction
SET recommended_visit_duration_minutes = CASE slug
    WHEN 'paring-hegyseg' THEN 120
    WHEN 'veka-szurdok' THEN 30
    WHEN 'vajdahunyadi-kastely' THEN 90
    WHEN 'oraljaboldogfalvi-reformatus-templom' THEN 30
    WHEN 'demsusi-kotemplom' THEN 30
    WHEN 'deva-vara' THEN 120
    WHEN 'algyogyfurdo' THEN 120
    WHEN 'gyulafehervar' THEN 120
    WHEN 'boli-barlang' THEN 60
    WHEN 'csernakereszturi-tajhaz' THEN 30
    ELSE recommended_visit_duration_minutes
END;
