-- Each project (job) carries a short reference code, set when the job is created. Two jobs must
-- never share a code, so a UNIQUE constraint is the last line of defence behind the server's own
-- duplicate check (CreateProject). Nullable so any row predating the column is left untouched
-- (H2 permits multiple NULLs under a unique constraint).
ALTER TABLE projects ADD COLUMN code VARCHAR(64);
ALTER TABLE projects ADD CONSTRAINT projects_code_unique UNIQUE (code);
