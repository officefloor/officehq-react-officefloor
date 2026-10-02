-- An invoice's amount is now DERIVED: it is the sum of its line items (quantity times unit price).
-- A newly raised invoice with no line items yet is worth $0.00, so the old "amount strictly
-- positive" invariant no longer holds. Drop that check; the amount column now caches the derived
-- total and is kept in step whenever a line item is added.
ALTER TABLE invoices DROP CONSTRAINT chk_invoices_amount_positive;
ALTER TABLE invoices ALTER COLUMN amount SET DEFAULT 0;
