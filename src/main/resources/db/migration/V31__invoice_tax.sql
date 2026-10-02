-- An invoice may carry a sales tax percentage, added on top after any discount. The invoice detail
-- shows the tax amount ((subtotal minus discount) times the percentage) and folds it into the final
-- total (subtotal minus discount plus tax). Existing invoices have no tax, so default to 0; the
-- stored invoice.amount column still means the undiscounted subtotal of its line items.
ALTER TABLE invoice ADD COLUMN tax_pct INT NOT NULL DEFAULT 0;
