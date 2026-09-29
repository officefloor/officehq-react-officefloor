-- Archive a client instead of deleting it: an archived client is retained but drops off the client
-- list and out of the client search. Defaults to not archived so existing rows (and normal creates)
-- stay visible.
ALTER TABLE clients ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
