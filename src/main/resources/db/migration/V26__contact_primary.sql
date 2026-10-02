-- Primary contact: each client has one main contact. An additive boolean flag on the existing
-- contact table (V10) marks the client's current main contact; existing rows default to not
-- primary. Changing the main contact clears the flag on the client's other contacts.
ALTER TABLE contact ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;
