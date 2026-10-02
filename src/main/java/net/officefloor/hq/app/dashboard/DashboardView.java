package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;

/** JSON response shape for the home dashboard: headline counts plus the outstanding money total. */
public class DashboardView {

    private final long clients;
    private final long projects;
    private final BigDecimal outstanding;
    private final long overdue;

    public DashboardView(long clients, long projects, BigDecimal outstanding, long overdue) {
        this.clients = clients;
        this.projects = projects;
        this.outstanding = outstanding;
        this.overdue = overdue;
    }

    public long getClients() {
        return clients;
    }

    public long getProjects() {
        return projects;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public long getOverdue() {
        return overdue;
    }
}
