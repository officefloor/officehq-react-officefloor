-- Each project (job) carries a short reference code set when it is created. No two jobs may share
-- one, so enforce uniqueness in the schema (mirrors the service and front-end guards). Nullable so
-- the add is safe on any existing rows; new projects always supply a code.
ALTER TABLE projects ADD COLUMN code VARCHAR(32);

ALTER TABLE projects
    ADD CONSTRAINT projects_code_unique UNIQUE (code);
