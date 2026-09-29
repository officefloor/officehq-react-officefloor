-- An invoice must bill a positive amount: zero/negative invoices are not allowed.
-- Additive: a new versioned migration alongside the existing invoices table.
ALTER TABLE invoices ADD CONSTRAINT invoices_amount_positive CHECK (amount > 0);
