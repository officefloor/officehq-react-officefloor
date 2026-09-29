-- An invoice must record a real charge: its amount has to be more than zero.
-- Enforced at the database as the final backstop behind the form and server validation.
ALTER TABLE invoices ADD CONSTRAINT invoices_amount_positive CHECK (amount > 0);
