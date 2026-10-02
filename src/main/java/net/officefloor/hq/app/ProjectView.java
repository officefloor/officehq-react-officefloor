package net.officefloor.hq.app;

/**
 * A project as the UI shows it: the project's id and name plus the owning client's id and NAME
 * (the list surfaces the client's name, not its id). Built by joining a project to its client.
 */
public class ProjectView {

    private final Long id;
    private final String name;
    private final Long clientId;
    private final String clientName;
    private final boolean archived;

    public ProjectView(Long id, String name, Long clientId, String clientName) {
        this(id, name, clientId, clientName, false);
    }

    public ProjectView(Long id, String name, Long clientId, String clientName, boolean archived) {
        this.id = id;
        this.name = name;
        this.clientId = clientId;
        this.clientName = clientName;
        this.archived = archived;
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

    public boolean isArchived() {
        return archived;
    }
}
