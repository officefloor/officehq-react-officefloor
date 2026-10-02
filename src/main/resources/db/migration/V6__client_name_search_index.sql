-- The client list has grown long enough to warrant searching by name. Back the name lookup with an
-- index so filtering stays quick as the table grows.
CREATE INDEX idx_clients_name ON clients (name);
