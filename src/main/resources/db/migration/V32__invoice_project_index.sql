-- Sorting clients by how much they owe sums, per client, each of that client's invoices (gathered
-- across all of their projects) net of its payments. That walk loads every project's invoices by
-- project_id, so index invoice by its owning project to keep the per-project lookup cheap as the
-- invoice table grows. Additive index on the existing invoice table (V4); no column or data change.
CREATE INDEX IF NOT EXISTS idx_invoice_project_id ON invoice (project_id);
