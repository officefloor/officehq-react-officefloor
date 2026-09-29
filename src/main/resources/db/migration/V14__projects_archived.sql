-- Archive a project instead of deleting it: an archived project is retained but drops off the
-- main project list and off the client's project list. Defaults to not archived so existing rows
-- (and normal creates) stay visible.
ALTER TABLE projects ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
