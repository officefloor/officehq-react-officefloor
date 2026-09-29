-- Every contact needs a proper email too. Enforce a minimal shape at the database as the last line
-- of defence, matching the UI/server rule (something@something.tld, no spaces) — same as clients.
ALTER TABLE contacts
    ADD CONSTRAINT contacts_email_valid
    CHECK (email LIKE '%_@_%._%' AND email NOT LIKE '% %');
