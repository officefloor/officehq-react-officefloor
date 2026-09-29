-- Two clients cannot share an email address: enforce uniqueness at the schema level (defence in
-- depth alongside the front-end and server checks, mirroring the V2 email-format constraint).
-- Additive: a new versioned migration adding a UNIQUE constraint on the existing email column.
ALTER TABLE clients
    ADD CONSTRAINT clients_email_unique UNIQUE (email);
