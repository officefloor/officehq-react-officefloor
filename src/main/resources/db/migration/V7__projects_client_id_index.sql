-- Opening a client lists the projects done for them, keyed by client_id. Index that lookup.
CREATE INDEX idx_projects_client_id ON projects (client_id);
