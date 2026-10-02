-- Invoices now carry a payment status. New invoices start UNPAID; marking one paid flips it to PAID.
ALTER TABLE invoices ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'UNPAID';
