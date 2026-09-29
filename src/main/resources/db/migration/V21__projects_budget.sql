-- A project can carry a budget: the money the owner has agreed to work to on it. Nullable — a
-- project may have no budget set yet — so no default; setting a budget writes the column. Additive:
-- a new versioned migration alongside the existing projects table.
ALTER TABLE projects ADD COLUMN budget NUMERIC(19, 2);
