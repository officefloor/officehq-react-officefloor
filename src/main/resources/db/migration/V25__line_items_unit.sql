-- A charge line now records the UNIT its quantity is counted in (e.g. "hours", "days", "items"), so
-- the line can read "2 hours" rather than a bare "2". Additive: a new column on the existing
-- line_items table. Existing rows have no unit recorded, so default to an empty string (NOT NULL so
-- the column always has a value to display).
ALTER TABLE line_items ADD COLUMN unit VARCHAR(50) NOT NULL DEFAULT '';
