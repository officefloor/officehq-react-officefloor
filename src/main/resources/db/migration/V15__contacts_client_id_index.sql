-- A client's page now shows how many contacts it has, counted by client_id. Index that lookup,
-- mirroring the projects count index (V7).
CREATE INDEX idx_contacts_client_id ON contacts (client_id);
