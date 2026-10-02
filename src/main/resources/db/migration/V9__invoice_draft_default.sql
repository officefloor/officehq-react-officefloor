-- Invoices now move through a lifecycle: DRAFT (just created) -> SENT (issued to the client) ->
-- PAID. A new invoice starts as a DRAFT, so change the status column's default accordingly. Additive
-- metadata change on the existing invoice table (V4/V5); existing rows keep their current status.
ALTER TABLE invoice ALTER COLUMN status SET DEFAULT 'DRAFT';
