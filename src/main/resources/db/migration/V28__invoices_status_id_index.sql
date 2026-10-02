-- The all-invoices list is now shown a page at a time: rows are selected in id order, optionally
-- narrowed to a lifecycle stage, with LIMIT/OFFSET. Index (status, id) so a filtered page stays a
-- cheap ordered range scan as the invoices table grows.
CREATE INDEX idx_invoices_status_id ON invoices (status, id);
