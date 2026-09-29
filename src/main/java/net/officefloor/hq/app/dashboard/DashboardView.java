package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;
import java.util.Map;

/** The home dashboard summary: how many clients and projects, money still owed, and how many SENT
 * invoices are overdue. Because different clients are paid in different currencies, money owed is
 * kept separate PER currency ({@link #outstandingByCurrency}) and never added across them. */
public class DashboardView {

    private final long clientsCount;
    private final long projectsCount;
    private final Map<String, BigDecimal> outstandingByCurrency;
    private final long overdueCount;

    public DashboardView(long clientsCount, long projectsCount,
            Map<String, BigDecimal> outstandingByCurrency, long overdueCount) {
        this.clientsCount = clientsCount;
        this.projectsCount = projectsCount;
        this.outstandingByCurrency = outstandingByCurrency;
        this.overdueCount = overdueCount;
    }

    public long getClientsCount() {
        return clientsCount;
    }

    public long getProjectsCount() {
        return projectsCount;
    }

    public Map<String, BigDecimal> getOutstandingByCurrency() {
        return outstandingByCurrency;
    }

    public long getOverdueCount() {
        return overdueCount;
    }
}
