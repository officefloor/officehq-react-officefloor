package net.officefloor.hq.app.client;

/** A client's at-a-glance counts: how many projects and contacts are kept for them. */
public class ClientSummaryView {

    private final long projectCount;
    private final long contactCount;

    public ClientSummaryView(long projectCount, long contactCount) {
        this.projectCount = projectCount;
        this.contactCount = contactCount;
    }

    public long getProjectCount() {
        return projectCount;
    }

    public long getContactCount() {
        return contactCount;
    }
}
