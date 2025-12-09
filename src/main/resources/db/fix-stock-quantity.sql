-- Fix paintings with stock_quantity = 0 or NULL
-- This ensures all paintings have at least 1 item in stock

UPDATE paintings 
SET stock_quantity = 1 
WHERE stock_quantity IS NULL OR stock_quantity < 1;

-- Verify the update
SELECT COUNT(*) as fixed_paintings 
FROM paintings 
WHERE stock_quantity >= 1;
