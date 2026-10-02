-- A client's page lists its projects filtered by lifecycle: active (status ACTIVE and not archived)
-- by default, with the finished and hidden ones revealed on demand. Index the client + status +
-- archived columns so that per-client filtered lookup stays cheap as the project table grows.
-- Additive index on the existing project table (V3/V15/V21); no data change.
CREATE INDEX IF NOT EXISTS idx_project_client_status_archived
    ON project (client_id, status, archived);
