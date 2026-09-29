-- An invoice's paid state is now worked out from the payments recorded against it (PARTIAL once
-- some of the amount is paid, PAID once it is covered), not flipped by hand. The stored status
-- column therefore only owns the issued lifecycle: DRAFT (not sent) vs SENT (sent). Normalise any
-- rows still carrying the old hand-set 'PAID' back to 'SENT' (they were issued) so the stored
-- column no longer contradicts the derived status. Additive: a new versioned migration alongside
-- the existing invoices table.
UPDATE invoices SET status = 'SENT' WHERE status = 'PAID';
