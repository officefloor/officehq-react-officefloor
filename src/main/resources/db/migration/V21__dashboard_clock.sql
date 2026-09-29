-- Dashboard reference date: the fixed "as of" date the home dashboard measures "overdue" against,
-- so the overdue count is deterministic in tests. A single row (id = 1) holds the date; when absent
-- the dashboard falls back to the real CURRENT_DATE. Seeded per spec via /__test__/seed's asOf.
CREATE TABLE dashboard_clock (
    id     INT PRIMARY KEY,
    as_of  DATE NOT NULL
);
