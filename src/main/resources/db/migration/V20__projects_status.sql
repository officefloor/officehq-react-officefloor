-- Projects carry a lifecycle status: ACTIVE, ON_HOLD or FINISHED. Additive: a new versioned
-- migration adding a text column that defaults to ACTIVE so all existing projects stay active,
-- with a CHECK constraint pinning it to the three known values.
ALTER TABLE projects ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE projects ADD CONSTRAINT projects_status_check
    CHECK (status IN ('ACTIVE', 'ON_HOLD', 'FINISHED'));
