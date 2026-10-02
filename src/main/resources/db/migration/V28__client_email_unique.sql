-- Two clients cannot share an email: enforce uniqueness at the schema level so a duplicate address
-- can never be persisted (defence-in-depth behind the server and UI checks). email is already
-- NOT NULL (V1) and format-checked (V2); this adds the uniqueness guarantee.
ALTER TABLE client
    ADD CONSTRAINT client_email_unique UNIQUE (email);
