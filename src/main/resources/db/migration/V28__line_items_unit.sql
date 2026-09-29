-- Charge lines now say what the quantity is measured in (unit), e.g. "hours". Additive: a new
-- versioned migration adding the column to the existing line_items table (V12). Existing rows get
-- an empty unit; the column is NOT NULL so every line carries a (possibly blank) unit.
ALTER TABLE line_items ADD COLUMN unit VARCHAR(50) NOT NULL DEFAULT '';
