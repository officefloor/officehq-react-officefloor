-- Main contact: each client has one contact marked as their primary. A boolean flag on the contact
-- row records it (the app keeps at most one TRUE per client). Defaults to not-primary so existing
-- rows and normal creates start unmarked; a client's primary is chosen explicitly.
ALTER TABLE contacts ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;
