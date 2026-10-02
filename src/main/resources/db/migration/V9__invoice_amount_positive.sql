-- An invoice must be raised for a real amount: the money owed has to be strictly positive, never
-- zero or negative. Enforce it at the schema level so no path can persist a worthless invoice.
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_amount_positive CHECK (amount > 0);
