-- An invoice may carry a percentage discount the owner takes off the subtotal (the sum of its line
-- items). The invoice detail shows the subtotal, the discount amount (subtotal times the percentage)
-- and the final total (subtotal minus the discount). Existing invoices have no discount, so default
-- to 0; the stored invoice.amount column keeps meaning the undiscounted subtotal of its line items.
ALTER TABLE invoice ADD COLUMN discount_pct INT NOT NULL DEFAULT 0;
