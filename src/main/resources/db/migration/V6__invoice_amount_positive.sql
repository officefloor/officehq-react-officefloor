-- An invoice must bill a real amount: enforce a positive amount at the schema level so an invoice
-- for zero (or a negative amount) can never be persisted (defence-in-depth behind the server and UI
-- validation). amount is already NOT NULL (V4); this adds the positivity check.
ALTER TABLE invoice
    ADD CONSTRAINT invoice_amount_positive
    CHECK (amount > 0);
