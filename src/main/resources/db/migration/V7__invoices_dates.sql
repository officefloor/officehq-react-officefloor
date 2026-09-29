-- Invoices carry two dates: issued_date (when the invoice went out) and due_date (when it is due).
-- Additive: a new versioned migration alongside the existing invoices table. Nullable, since dates
-- are supplied by the seed / callers and older rows may not have them.
ALTER TABLE invoices ADD COLUMN issued_date DATE;
ALTER TABLE invoices ADD COLUMN due_date DATE;
