-- Invoices carry a payment status (default UNPAID). Marking an invoice paid flips this to PAID.
-- Additive: a new versioned migration alongside the existing invoices table.
ALTER TABLE invoices ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'UNPAID';
