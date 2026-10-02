-- Invoices gain two dates the owner tracks: the date the invoice was issued (sent out) and the date
-- payment is due. Additive columns on the existing invoice table (V4); nullable so existing rows and
-- the create-by-amount path (which does not set dates) remain valid.
ALTER TABLE invoice ADD COLUMN issued_date DATE;
ALTER TABLE invoice ADD COLUMN due_date DATE;
