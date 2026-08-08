UPDATE price_item
SET active = FALSE
WHERE code IN ('lunch', 'full_board');
