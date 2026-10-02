-- Invoices now move through a lifecycle: DRAFT -> SENT -> PAID. A newly created invoice starts as a
-- DRAFT; it becomes SENT when it is sent out, and PAID once payment is taken. New rows default to
-- DRAFT going forward.
ALTER TABLE invoices ALTER COLUMN status SET DEFAULT 'DRAFT';
