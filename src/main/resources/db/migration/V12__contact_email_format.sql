-- Every contact needs a proper email: enforce a basic email shape at the schema level so a contact
-- with a blank or malformed address can never be persisted (defence-in-depth behind the server and
-- UI validation, mirroring the client check in V2). email is already NOT NULL (V10); this adds the
-- format check.
ALTER TABLE contact
    ADD CONSTRAINT contact_email_format
    CHECK (REGEXP_LIKE(email, '^[^\s@]+@[^\s@]+\.[^\s@]+$'));
