-- The home dashboard totals money still owed by summing invoices whose status is UNPAID. Index the
-- status column so that outstanding-total lookup stays quick as the invoices table grows.
CREATE INDEX idx_invoices_status ON invoices (status);
