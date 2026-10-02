-- A charge line now says not just how many, but the unit that quantity is counted in (e.g. "hours",
-- "days", "items"). Record it alongside the quantity on the line item. Existing rows predate the
-- unit, so default to a generic "unit" to keep the NOT NULL invariant.
ALTER TABLE line_items ADD COLUMN unit VARCHAR(32) NOT NULL DEFAULT 'unit';
