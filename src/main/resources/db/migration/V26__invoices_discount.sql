-- An invoice can carry a percentage discount taken off its subtotal (the sum of its line items).
-- The final total is the subtotal less that discount. Additive: a new column on the existing
-- invoices table. Existing invoices have no discount, so default to 0 (NOT NULL so the column
-- always has a value to compute with). Stored as a whole/fractional percent (e.g. 10 = 10%).
ALTER TABLE invoices ADD COLUMN discount_pct DECIMAL(5, 2) NOT NULL DEFAULT 0;
