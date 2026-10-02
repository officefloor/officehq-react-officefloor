-- Global search now looks across projects by name too (alongside clients, V6). Back the project
-- name lookup with an index so the cross-entity search stays quick as the table grows.
CREATE INDEX idx_projects_name ON projects (name);
