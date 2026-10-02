-- A client's email is their unique handle: two clients can never share one. Enforce it in the
-- schema so a duplicate can never be persisted regardless of entry point (mirrors the service and
-- front-end guards).
ALTER TABLE clients
    ADD CONSTRAINT clients_email_unique UNIQUE (email);
