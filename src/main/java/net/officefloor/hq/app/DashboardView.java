package net.officefloor.hq.app;

import java.math.BigDecimal;

/** The home dashboard summary: how many clients and projects there are, and the total still owed. */
public class DashboardView {

    private final long clientsCount;
    private final long projectsCount;
    private final BigDecimal outstandingTotal;

    public DashboardView(long clientsCount, long projectsCount, BigDecimal outstandingTotal) {
        this.clientsCount = clientsCount;
        this.projectsCount = projectsCount;
        this.outstandingTotal = outstandingTotal;
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
}
