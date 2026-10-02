-- A client's page now lists their ACTIVE, non-archived projects by default. Index the columns that
-- lookup filters by so the default view stays cheap as a client accrues finished/archived work.
CREATE INDEX idx_projects_client_status_archived ON projects (client_id, status, archived);
