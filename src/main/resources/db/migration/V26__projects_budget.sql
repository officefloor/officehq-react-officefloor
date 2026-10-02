-- A project can carry a BUDGET: the money (two decimal places) the user plans to invoice against it.
-- Defaults 0 so every existing project starts with no budget set; setting a budget raises it.
ALTER TABLE projects ADD COLUMN budget NUMERIC(12, 2) NOT NULL DEFAULT 0;
