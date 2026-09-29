package net.officefloor.hq.app.project;

/** A project joined with its client's name — what the projects list surfaces. */
public class ProjectView {

    private final Long id;
    private final String name;
    private final Long clientId;
    private final String clientName;

    public ProjectView(Long id, String name, Long clientId, String clientName) {
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
