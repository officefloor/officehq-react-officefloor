package net.officefloor.hq.app.dashboard;

import java.util.List;

/**
 * JSON response shape for the home dashboard: headline counts, the outstanding money total, and the
 * top clients ranked by how much they owe.
 */
public class DashboardView {

    private final long clients;
    private final long projects;
    // Outstanding money kept separate per currency — the totals are never added across currencies.
    private final List<CurrencyTotalView> outstanding;
    private final long overdue;
    private final List<TopClientView> topClients;

    public DashboardView(long clients, long projects, List<CurrencyTotalView> outstanding,
            long overdue, List<TopClientView> topClients) {
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

    public List<CurrencyTotalView> getOutstanding() {
        return outstanding;
    }

    public long getOverdue() {
        return overdue;
    }

    public List<TopClientView> getTopClients() {
        return topClients;
    }
}
