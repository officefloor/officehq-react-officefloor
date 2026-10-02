-- Every client needs a proper email: enforce a basic email shape at the schema level so a client
-- with a blank or malformed address can never be persisted (defence-in-depth behind the server and
-- UI validation). email is already NOT NULL (V1); this adds the format check.
ALTER TABLE client
    ADD CONSTRAINT client_email_format
    CHECK (REGEXP_LIKE(email, '^[^\s@]+@[^\s@]+\.[^\s@]+$'));
