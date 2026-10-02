package net.officefloor.hq.app.projects;

/** JSON response shape for a project (what the UI renders): carries the client's NAME, not just id. */
public class ProjectView {

    private final long id;
    private final String name;
    private final long clientId;
    private final String clientName;

    public ProjectView(long id, String name, long clientId, String clientName) {
        this.id = id;
        this.name = name;
        this.clientId = clientId;
        this.clientName = clientName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }
}
