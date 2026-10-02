package net.officefloor.hq.app;

import java.util.List;

/**
 * The result of one global search: the matching clients and the matching projects, kept as two
 * groups so the UI can show each kind under its own heading.
 */
public class SearchView {

    private final List<Client> clients;
    private final List<ProjectView> projects;

    public SearchView(List<Client> clients, List<ProjectView> projects) {
        this.clients = clients;
        this.projects = projects;
    }

    public List<Client> getClients() {
        return clients;
    }

    public List<ProjectView> getProjects() {
        return projects;
    }
}
