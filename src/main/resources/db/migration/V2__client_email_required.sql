-- Every client needs a proper email address: enforce it in the schema too, so a bad address can
-- never be persisted regardless of entry point. Mirrors the front-end and service guards.
ALTER TABLE clients
    ADD CONSTRAINT clients_email_format
    CHECK (REGEXP_LIKE(email, '^[^\s@]+@[^\s@]+\.[^\s@]+$'));
