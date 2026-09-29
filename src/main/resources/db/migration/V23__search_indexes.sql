-- Global search feature: one search box looks across both clients and projects by name. Each match
-- is a case-insensitive substring lookup on the entity's name, so index the name columns the search
-- scans. Additive and non-destructive — no data or column changes.
CREATE INDEX idx_clients_name ON clients(name);
CREATE INDEX idx_projects_name ON projects(name);
