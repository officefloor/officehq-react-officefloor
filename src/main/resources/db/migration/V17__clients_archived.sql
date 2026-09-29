-- Clients can be archived (tucked away) rather than deleted: an archived client drops off the main
-- client list and the search, but its row (and everything it owns) is retained. Additive: a new
-- versioned migration that adds a boolean flag defaulting to FALSE so all existing clients stay
-- visible.
ALTER TABLE clients ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
