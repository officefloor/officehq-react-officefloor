-- Every client needs a proper email. Enforce a minimal shape at the database as the last line of
-- defence, matching the UI/server rule (something@something.tld, no spaces).
ALTER TABLE clients
    ADD CONSTRAINT clients_email_valid
    CHECK (email LIKE '%_@_%._%' AND email NOT LIKE '% %');
