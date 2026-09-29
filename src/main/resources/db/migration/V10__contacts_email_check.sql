-- Contacts need a proper email address too: enforce a basic well-formed email at the schema level
-- (defence in depth alongside the front-end and server checks), mirroring V2 for clients.
-- Additive: a new versioned migration.
ALTER TABLE contacts
    ADD CONSTRAINT contacts_email_format
    CHECK (REGEXP_LIKE(email, '^[^@\s]+@[^@\s]+\.[^@\s]+$'));
