-- Client at-a-glance counts feature: a client's page shows how many projects and contacts it has.
-- Those counts are per-client COUNT(*) lookups keyed by client_id, so index the foreign keys they
-- scan. Additive and non-destructive — no data or column changes.
CREATE INDEX idx_projects_client_id ON projects(client_id);
CREATE INDEX idx_contacts_client_id ON contacts(client_id);
