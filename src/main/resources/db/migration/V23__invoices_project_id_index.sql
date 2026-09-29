-- The all-invoices list (GET /api/invoices/all) is now paged as it has grown large. Each page joins
-- every invoice to its project and orders by invoice id; index the project FK so paging that join
-- stays cheap. Additive: a new versioned migration, no change to an applied one.
CREATE INDEX IF NOT EXISTS idx_invoices_project_id ON invoices(project_id);
