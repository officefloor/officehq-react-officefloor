package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * The home dashboard summary: how many clients and projects there are, the total still owed, and how
 * many SENT invoices are overdue (past their due date as of the dashboard's reference date).
 */
public class DashboardView {

    private final long clientsCount;
    private final long projectsCount;
    private final BigDecimal outstandingTotal;
    private final long overdueCount;

    public DashboardView(long clientsCount, long projectsCount, BigDecimal outstandingTotal,
            long overdueCount) {
        this.clientsCount = clientsCount;
        this.projectsCount = projectsCount;
        this.outstandingTotal = outstandingTotal;
        this.overdueCount = overdueCount;
    }

    public long getClientsCount() {
        return clientsCount;
    }

    public long getProjectsCount() {
        return projectsCount;
    }

    public BigDecimal getOutstandingTotal() {
        return outstandingTotal;
    }

    public long getOverdueCount() {
        return overdueCount;
    }
}
