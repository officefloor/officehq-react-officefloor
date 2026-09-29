-- Invoice lifecycle: an invoice starts life as a DRAFT, then is SENT, then PAID. New invoices
-- default to DRAFT so a freshly created invoice begins in the draft stage before it is sent.
ALTER TABLE invoices ALTER COLUMN status SET DEFAULT 'DRAFT';
