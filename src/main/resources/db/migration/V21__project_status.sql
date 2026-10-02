-- Project lifecycle status: the owner marks a project ACTIVE, ON_HOLD or FINISHED and the list shows
-- which it is. Additive column on the existing project table (V3); existing rows default to ACTIVE.
ALTER TABLE project ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';
