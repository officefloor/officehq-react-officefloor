package net.officefloor.hq.app;

import java.util.List;

/**
 * A project as the UI shows it: the project's id and name plus the owning client's id and NAME
 * (the list surfaces the client's name, not its id). Built by joining a project to its client. Also
 * carries the ids of the tags attached to it, so the list can be filtered by tag.
 */
public class ProjectView {

    private final Long id;
    private final String name;
    private final Long clientId;
    private final String clientName;
    private final boolean archived;
    private final String status;
    private final List<Long> tagIds;

    public ProjectView(Long id, String name, Long clientId, String clientName) {
        this(id, name, clientId, clientName, false, "ACTIVE", List.of());
    }

    public ProjectView(Long id, String name, Long clientId, String clientName, String status) {
        this(id, name, clientId, clientName, false, status, List.of());
    }

    public ProjectView(Long id, String name, Long clientId, String clientName, boolean archived,
            String status, List<Long> tagIds) {
        this.id = id;
        this.name = name;
        this.clientId = clientId;
        this.clientName = clientName;
        this.archived = archived;
        this.status = status;
        this.tagIds = tagIds;
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

    public String getStatus() {
        return status;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }
}
