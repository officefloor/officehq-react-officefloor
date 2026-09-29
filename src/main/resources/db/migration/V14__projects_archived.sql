-- Projects can be archived (soft-hidden) rather than deleted: an archived project drops off the
-- lists but its row (and everything it owns) is retained. Additive: a new versioned migration that
-- adds a boolean flag defaulting to FALSE so all existing projects stay visible.
ALTER TABLE projects ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
