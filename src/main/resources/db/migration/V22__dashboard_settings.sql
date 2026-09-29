-- The dashboard measures "overdue" against a fixed reference date so the count is deterministic
-- (the harness seeds this as `asOf`). A single-row settings table holds that date; when unset the
-- dashboard falls back to the current date. Additive: a new versioned migration.
CREATE TABLE dashboard_settings (
    id    INTEGER PRIMARY KEY,
    as_of DATE
);
