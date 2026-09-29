package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * Cross-feature aggregate for the home dashboard: how many clients and projects exist, and the
 * total still owed (sum of UNPAID invoice amounts across all projects). Serialised as JSON by
 * GET /api/dashboard.
 */
public record DashboardSummary(long clientsCount, long projectsCount, BigDecimal outstandingTotal) {
}
