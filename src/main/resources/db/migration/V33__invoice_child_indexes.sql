-- Ranking clients by how much they owe sums each invoice's line items and payments (see
-- DashboardRepository#topClients / ClientOutstandingRepository). Those per-invoice aggregates look
-- up child rows by invoice_id, so index the FK columns to keep the roll-up cheap as invoices grow.
-- Additive: a new versioned migration alongside the existing line_items and payments tables.
CREATE INDEX IF NOT EXISTS idx_line_items_invoice_id ON line_items(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_invoice_id ON payments(invoice_id);
