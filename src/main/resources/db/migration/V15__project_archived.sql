-- Archiving a project: rather than deleting it, the owner can tuck a project away so it drops off
-- their lists while being retained. Additive metadata flag on the existing project table (V3);
-- existing rows default to not archived.
ALTER TABLE project ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
