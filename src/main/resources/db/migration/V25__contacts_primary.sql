-- A client has one main (primary) contact. Additive boolean flag on contacts; at most one row per
-- client is TRUE — the app clears the others when a new primary is chosen. Existing rows default to
-- FALSE (no primary picked yet).
ALTER TABLE contacts ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;
