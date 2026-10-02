-- Invoices gain a payment status so the owner can mark one paid. New invoices default to UNPAID;
-- paying an invoice flips it to PAID (and audits the transition). Additive column on the existing
-- invoice table (V4); existing rows backfill to UNPAID.
ALTER TABLE invoice ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'UNPAID';
