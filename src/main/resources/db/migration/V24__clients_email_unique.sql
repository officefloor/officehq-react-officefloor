-- Two clients cannot share an email. Enforce uniqueness at the database as the last line of
-- defence, matching the UI/server rule (the server rejects a duplicate before it reaches here).
ALTER TABLE clients
    ADD CONSTRAINT clients_email_unique UNIQUE (email);
