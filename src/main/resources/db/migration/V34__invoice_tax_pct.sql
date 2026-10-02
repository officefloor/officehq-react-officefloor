-- An invoice can now carry a sales tax percentage, applied on top of the amount left after the
-- discount (subtotal minus discount). The stored amount stays the subtotal; the discount, tax and
-- final total are derived from it. Existing invoices predate the tax, so default to 0 (no tax). The
-- percentage is bounded to the 0..100 range so no path can persist a nonsensical rate.
ALTER TABLE invoices ADD COLUMN tax_pct NUMERIC(5, 2) NOT NULL DEFAULT 0;
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_tax_pct_range
    CHECK (tax_pct >= 0 AND tax_pct <= 100);
