-- Project budget: the amount the user has agreed to spend on a project, so the project detail can
-- show the budget, how much has been invoiced against it, and what is left. Nullable — a project
-- has no budget until one is set; existing rows start with none.
ALTER TABLE projects ADD COLUMN budget DECIMAL(12, 2);
