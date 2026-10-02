-- Project budget: the owner sets a budget on a project and the detail compares it against what has
-- been invoiced. Additive column on the existing project table (V3); existing rows default to 0.
ALTER TABLE project ADD COLUMN budget DECIMAL(19, 2) NOT NULL DEFAULT 0;
