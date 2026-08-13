INSERT INTO guesthouse_notification_recipient (
    id,
    guesthouse_id,
    email,
    active
)
SELECT
    recipient.id,
    guesthouse.id,
    'nistorzsolt5@gmail.com',
    TRUE
FROM (
    VALUES
        ('nisztor-panzio', 'd66cc001-6092-467a-9812-2cfd95657e05'::UUID),
        ('bukovina-panzio', 'd66cc002-6092-467a-9812-2cfd95657e05'::UUID)
) AS recipient(guesthouse_slug, id)
JOIN guesthouse ON guesthouse.slug = recipient.guesthouse_slug
WHERE NOT EXISTS (
    SELECT 1
    FROM guesthouse_notification_recipient existing
    WHERE existing.guesthouse_id = guesthouse.id
      AND LOWER(existing.email) = 'nistorzsolt5@gmail.com'
);

UPDATE guesthouse_notification_recipient
SET active = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE LOWER(email) = 'nistorzsolt5@gmail.com'
  AND guesthouse_id IN (
      SELECT id
      FROM guesthouse
      WHERE slug IN ('nisztor-panzio', 'bukovina-panzio')
  );
