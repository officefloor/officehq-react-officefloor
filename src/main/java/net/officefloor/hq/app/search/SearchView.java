package net.officefloor.hq.app.search;

import java.util.List;

/** The result of one global search: the matching clients and projects, grouped by kind. */
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
