-- Archiving a client: rather than deleting it, the owner can tuck a client away so it drops off
-- their list and search while being retained. Additive metadata flag on the existing client table
-- (V1); existing rows default to not archived.
ALTER TABLE client ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
