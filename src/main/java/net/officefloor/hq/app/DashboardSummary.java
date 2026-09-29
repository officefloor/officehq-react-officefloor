package net.officefloor.hq.app;

import java.util.List;

/**
 * Cross-feature aggregate for the home dashboard: how many clients and projects exist, the money
 * still owed broken out per currency (clients are paid in different currencies, which are never
 * added together), and how many SENT invoices are overdue (past their due date as of the dashboard's
 * reference date). Serialised as JSON by GET /api/dashboard.
 */
public record DashboardSummary(long clientsCount, long projectsCount,
        List<OutstandingByCurrency> outstanding, long overdueCount) {
}
