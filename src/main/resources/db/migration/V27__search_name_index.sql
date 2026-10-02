-- One global search box looks across clients and projects by name (case-insensitive contains).
-- Index the name columns so that lookup stays cheap as both tables grow. Additive indexes on the
-- existing client (V1) and project (V3) tables; no data change.
CREATE INDEX IF NOT EXISTS idx_client_name ON client (name);
CREATE INDEX IF NOT EXISTS idx_project_name ON project (name);
