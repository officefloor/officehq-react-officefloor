-- An invoice can now carry a percentage discount taken off its subtotal (the sum of its line
-- items). The stored amount stays the subtotal; the discount and final total are derived from it.
-- Existing invoices predate the discount, so default to 0 (no discount). The percentage is bounded
-- to the 0..100 range so no path can persist a nonsensical discount.
ALTER TABLE invoices ADD COLUMN discount_pct NUMERIC(5, 2) NOT NULL DEFAULT 0;
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_discount_pct_range
    CHECK (discount_pct >= 0 AND discount_pct <= 100);
