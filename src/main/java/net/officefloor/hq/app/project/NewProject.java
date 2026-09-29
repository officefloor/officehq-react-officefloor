package net.officefloor.hq.app.project;

/** Request body for creating a project: a name and the client it is for. */
public class NewProject {

    private String name;

    private Long clientId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
