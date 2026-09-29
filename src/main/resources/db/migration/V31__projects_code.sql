-- Each project (job) carries a short reference code, set when the job is created, that no two jobs
-- may share. Additive: a new versioned migration adding a nullable text column (existing rows carry
-- no code) with a UNIQUE constraint so codes stay distinct across jobs.
ALTER TABLE projects ADD COLUMN code VARCHAR(32);
ALTER TABLE projects ADD CONSTRAINT projects_code_unique UNIQUE (code);
