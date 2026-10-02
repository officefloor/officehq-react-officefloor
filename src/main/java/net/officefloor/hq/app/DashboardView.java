package net.officefloor.hq.app;

import java.util.List;

/**
 * The home dashboard summary: how many clients and projects there are, the money still owed kept
 * separate per currency (different currencies are never added together, V35), and how many SENT
 * invoices are overdue (past their due date as of the dashboard's reference date).
 */
public class DashboardView {

    private final long clientsCount;
    private final long projectsCount;
    private final List<CurrencyAmountView> outstanding;
    private final long overdueCount;

    public DashboardView(long clientsCount, long projectsCount,
            List<CurrencyAmountView> outstanding, long overdueCount) {
        this.clientsCount = clientsCount;
        this.projectsCount = projectsCount;
        this.outstanding = outstanding;
        this.overdueCount = overdueCount;
    }

    public long getClientsCount() {
        return clientsCount;
    }

    public long getProjectsCount() {
        return projectsCount;
    }

    public List<CurrencyAmountView> getOutstanding() {
        return outstanding;
    }

    public long getOverdueCount() {
        return overdueCount;
    }
}
