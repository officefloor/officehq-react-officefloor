-- The one global search box (GET /api/search) matches clients and projects by a case-insensitive
-- substring of their name. Index each name column so the search stays cheap as the lists grow.
-- Additive: a new versioned migration, no change to an applied one.
CREATE INDEX IF NOT EXISTS idx_clients_name ON clients(name);
CREATE INDEX IF NOT EXISTS idx_projects_name ON projects(name);
