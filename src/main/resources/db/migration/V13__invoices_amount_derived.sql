-- An invoice's amount is now derived from its line items (the sum of qty * unit_price), not typed
-- in directly. Relax the stored amount column so an invoice can exist before it has any line items
-- (a total of zero): default it to 0 and drop the positive-amount check. Additive: a new versioned
-- migration alongside the existing invoices table.
ALTER TABLE invoices ALTER COLUMN amount SET DEFAULT 0;
ALTER TABLE invoices DROP CONSTRAINT invoices_amount_positive;
