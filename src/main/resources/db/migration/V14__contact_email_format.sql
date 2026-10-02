-- Every contact needs a proper email address too: enforce it in the schema, so a bad address can
-- never be persisted regardless of entry point. Mirrors the front-end and service guards (and the
-- clients_email_format constraint).
ALTER TABLE contacts
    ADD CONSTRAINT contacts_email_format
    CHECK (REGEXP_LIKE(email, '^[^\s@]+@[^\s@]+\.[^\s@]+$'));
