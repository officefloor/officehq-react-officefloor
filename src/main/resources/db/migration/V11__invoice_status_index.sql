-- The dashboard totals the money owed by reading every SENT invoice across all projects (grouped by
-- status). Add a supporting index so that status lookup stays cheap as invoices accumulate. Additive
-- and non-destructive: no column or constraint change, just a new index on the existing invoice table.
CREATE INDEX idx_invoice_status ON invoice (status);
