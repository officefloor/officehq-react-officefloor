-- A project's invoices can now be sorted by their due date (earliest due first). Index the due_date
-- column so that ordered lookup stays quick as the invoices table grows.
CREATE INDEX idx_invoices_due_date ON invoices (due_date);
