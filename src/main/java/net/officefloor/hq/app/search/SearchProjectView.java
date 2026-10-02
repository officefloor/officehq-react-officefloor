package net.officefloor.hq.app.search;

/** JSON response shape for a project matched by the global search (carries its client's name). */
public class SearchProjectView {

    private final long id;
    private final String name;
    private final long clientId;
    private final String clientName;

    public SearchProjectView(long id, String name, long clientId, String clientName) {
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
