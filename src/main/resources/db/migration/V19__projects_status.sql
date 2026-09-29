-- Project status: a project is ACTIVE, ON_HOLD or FINISHED so the user can track where it stands.
-- Defaults to ACTIVE so existing rows (and normal creates that omit it) start active.
ALTER TABLE projects ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
    CHECK (status IN ('ACTIVE', 'ON_HOLD', 'FINISHED'));
