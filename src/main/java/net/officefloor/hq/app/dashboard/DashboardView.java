package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;
import java.util.List;

/**
 * JSON response shape for the home dashboard: headline counts, the outstanding money total, and the
 * top clients ranked by how much they owe.
 */
public class DashboardView {

    private final long clients;
    private final long projects;
    private final BigDecimal outstanding;
    private final long overdue;
    private final List<TopClientView> topClients;

    public DashboardView(long clients, long projects, BigDecimal outstanding, long overdue,
            List<TopClientView> topClients) {
        this.clients = clients;
        this.projects = projects;
        this.outstanding = outstanding;
        this.overdue = overdue;
        this.topClients = topClients;
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

    public List<TopClientView> getTopClients() {
        return topClients;
    }
}
