package net.officefloor.hq.app;

/** Request body for creating a project: the name the owner types and the chosen client's id. */
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
