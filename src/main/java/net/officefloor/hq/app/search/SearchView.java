package net.officefloor.hq.app.search;

import java.util.List;

/**
 * JSON response shape for the global search: matches grouped by kind — the clients whose name
 * matches and the projects whose name matches, gathered under one query.
 */
public class SearchView {

    private final List<SearchClientView> clients;
    private final List<SearchProjectView> projects;

    public SearchView(List<SearchClientView> clients, List<SearchProjectView> projects) {
        this.clients = clients;
        this.projects = projects;
    }

    public List<SearchClientView> getClients() {
        return clients;
    }

    public List<SearchProjectView> getProjects() {
        return projects;
    }
}
