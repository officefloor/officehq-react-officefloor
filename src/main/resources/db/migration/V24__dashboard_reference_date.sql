-- Dashboard reference date: "overdue" is measured against a fixed as-of date the dashboard is given,
-- so the overdue count is deterministic in tests rather than depending on the wall clock. A single
-- config row (id = 1) holds that date; when absent the dashboard falls back to the current date.
CREATE TABLE dashboard_config (
    id    BIGINT PRIMARY KEY,
    as_of DATE
);
