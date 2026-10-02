-- Projects now carry a lifecycle status: whether the work is ACTIVE, ON_HOLD or FINISHED. New
-- projects start ACTIVE; the default keeps every existing project visible as active work.
ALTER TABLE projects ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';
