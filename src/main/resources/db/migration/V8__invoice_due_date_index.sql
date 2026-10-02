-- Sorting a project's invoices by due date reads them ordered by (project_id, due_date). Add a
-- supporting index so the due-date sort stays cheap as a project accrues invoices. Additive and
-- non-destructive: no column or constraint change, just a new index on the existing invoice table.
CREATE INDEX idx_invoice_project_due_date ON invoice (project_id, due_date);
