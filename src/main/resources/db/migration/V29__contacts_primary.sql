-- A client has one main (primary) contact. Flag it on the contact row rather than as a separate
-- column on the client, so the "who is primary" answer lives with the contact it names. Defaults
-- FALSE so existing contacts are non-primary; picking a main contact flips exactly one to TRUE.
ALTER TABLE contacts ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;
