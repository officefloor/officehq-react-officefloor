-- Invoice dates: an invoice records when it was issued (went out) and when it is due. Both default
-- so existing rows and normal creates start with a sensible date; the due date defaults 30 days on.
ALTER TABLE invoices ADD COLUMN issued_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE invoices ADD COLUMN due_date DATE NOT NULL DEFAULT DATEADD('DAY', 30, CURRENT_DATE);
