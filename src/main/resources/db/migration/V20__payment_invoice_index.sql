-- An invoice's payment status is now derived from its payments: each read of a project's invoices
-- (and of a single opened invoice) sums that invoice's payments to work out whether it is PAID,
-- PARTIAL or still owing. Index payment by its owning invoice so those per-invoice aggregations stay
-- cheap. Additive index on the existing payment table (V19); no column or data change.
CREATE INDEX idx_payment_invoice_id ON payment (invoice_id);
