-- Each project carries a short reference code, set when it is created, unique across all projects so
-- no two projects share one (mirrors the client email uniqueness in V28). Enforce uniqueness at the
-- schema level as defence-in-depth behind the server and UI checks.
ALTER TABLE project
    ADD COLUMN code VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE project
    ADD CONSTRAINT project_code_unique UNIQUE (code);
