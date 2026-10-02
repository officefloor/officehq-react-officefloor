-- Clients are archived, not deleted: a flag tucks a client away so it drops off the list and the
-- search while the row (and everything hanging off it) is retained. Defaults FALSE so every existing
-- client stays visible; archiving flips it to TRUE.
ALTER TABLE clients ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
