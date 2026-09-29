-- An invoice can carry a sales tax added on top, worked out after any discount. tax_pct is that
-- percentage (0..100); the final total is (subtotal minus the discount) times (1 + tax_pct/100).
-- Additive: a new versioned migration alongside the existing invoices table. Existing rows get 0
-- (no tax); the column is NOT NULL so every invoice carries a (possibly zero) tax rate.
ALTER TABLE invoices ADD COLUMN tax_pct NUMERIC(5, 2) NOT NULL DEFAULT 0
    CHECK (tax_pct >= 0 AND tax_pct <= 100);
