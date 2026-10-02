-- Invoices now carry two dates: when the invoice was issued (sent out) and when payment is due.
-- Existing rows get today's date as a sensible default; both are required going forward.
ALTER TABLE invoices ADD COLUMN issued_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE invoices ADD COLUMN due_date DATE NOT NULL DEFAULT CURRENT_DATE;
