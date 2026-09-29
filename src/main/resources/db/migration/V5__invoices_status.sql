-- Invoice payment: an invoice carries a status so it can be marked paid. Defaults to UNPAID so
-- existing rows and normal creates start unpaid; marking paid flips it to PAID.
ALTER TABLE invoices ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'UNPAID';
