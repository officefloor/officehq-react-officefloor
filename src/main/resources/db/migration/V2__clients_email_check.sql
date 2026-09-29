-- Every client needs a proper email address: enforce a basic well-formed email at the schema level
-- (defence in depth alongside the front-end and server checks). Additive: a new versioned migration.
ALTER TABLE clients
    ADD CONSTRAINT clients_email_format
    CHECK (REGEXP_LIKE(email, '^[^@\s]+@[^@\s]+\.[^@\s]+$'));
