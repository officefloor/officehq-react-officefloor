-- Projects are archived, not deleted: a flag tucks a project away so it drops off the lists while the
-- row (and everything hanging off it) is retained. Defaults FALSE so every existing project stays
-- visible; archiving flips it to TRUE.
ALTER TABLE projects ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
