-- Invoices move through a lifecycle: DRAFT (default for a new invoice) -> SENT -> PAID.
-- Additive: a new versioned migration that switches the default status from UNPAID to DRAFT.
ALTER TABLE invoices ALTER COLUMN status SET DEFAULT 'DRAFT';
