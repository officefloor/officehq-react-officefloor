package net.officefloor.hq.app;

/** A client's at-a-glance counts: how many projects and contacts that client has. */
public class ClientSummaryView {

    private final long projectsCount;
    private final long contactsCount;

    public ClientSummaryView(long projectsCount, long contactsCount) {
        this.projectsCount = projectsCount;
        this.contactsCount = contactsCount;
    }

    public long getProjectsCount() {
        return projectsCount;
    }

    public long getContactsCount() {
        return contactsCount;
    }
}
