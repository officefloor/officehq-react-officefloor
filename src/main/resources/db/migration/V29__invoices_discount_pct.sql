-- An invoice can carry a percentage discount taken off its subtotal (the sum of its line items).
-- discount_pct is that percentage (0..100); the final total is subtotal minus subtotal * pct/100.
-- Additive: a new versioned migration alongside the existing invoices table. Existing rows get 0
-- (no discount); the column is NOT NULL so every invoice carries a (possibly zero) discount.
ALTER TABLE invoices ADD COLUMN discount_pct NUMERIC(5, 2) NOT NULL DEFAULT 0
    CHECK (discount_pct >= 0 AND discount_pct <= 100);
