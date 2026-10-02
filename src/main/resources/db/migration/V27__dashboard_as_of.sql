-- The home dashboard reports how many SENT invoices are overdue. "Overdue" is measured against a
-- fixed reference date (as_of) so the count stays deterministic in tests. This single-row settings
-- table holds that date; when no row is present the dashboard falls back to the current date.
CREATE TABLE dashboard_settings (
    id    INT  NOT NULL PRIMARY KEY,
    as_of DATE
);
