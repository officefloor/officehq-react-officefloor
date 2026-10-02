package net.officefloor.hq.app.clients;

/** JSON response shape for a client's at-a-glance counts (projects and contacts). */
public class ClientCountsView {

    private final long projects;
    private final long contacts;

    public ClientCountsView(long projects, long contacts) {
        this.projects = projects;
        this.contacts = contacts;
    }

    public long getProjects() {
        return projects;
    }

    public long getContacts() {
        return contacts;
    }
}
