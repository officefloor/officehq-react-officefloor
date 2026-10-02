-- A charge line now records the unit its quantity is measured in (e.g. "hours", "items") alongside
-- the quantity and unit price, so the line reads as "2 hours" rather than a bare "2". Existing rows
-- have no unit yet, so default to the empty string; the line total is unaffected (quantity times
-- unit price as before).
ALTER TABLE line_item ADD COLUMN unit VARCHAR(50) NOT NULL DEFAULT '';
