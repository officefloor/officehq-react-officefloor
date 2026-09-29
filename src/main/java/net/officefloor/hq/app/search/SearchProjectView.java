package net.officefloor.hq.app.search;

/** A project that matched a global search — joined with its client's name, as the results surface. */
public class SearchProjectView {

    private final Long id;
    private final String name;
    private final Long clientId;
    private final String clientName;

    public SearchProjectView(Long id, String name, Long clientId, String clientName) {
        this.id = id;
        this.name = name;
        this.clientId = clientId;
        this.clientName = clientName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }
}
