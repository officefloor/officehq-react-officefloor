package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * Cross-feature aggregate for the home dashboard: how many clients and projects exist, the total
 * still owed (sum of UNPAID invoice amounts across all projects), and how many SENT invoices are
 * overdue (past their due date as of the dashboard's reference date). Serialised as JSON by
 * GET /api/dashboard.
 */
public record DashboardSummary(long clientsCount, long projectsCount, BigDecimal outstandingTotal,
        long overdueCount) {
}
