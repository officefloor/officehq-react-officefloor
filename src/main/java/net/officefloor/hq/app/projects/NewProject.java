package net.officefloor.hq.app.projects;

import net.officefloor.web.HttpObject;

/** JSON request body for creating a project. {@link HttpObject} loads it from the request entity. */
@HttpObject
public class NewProject {

    private String name;
    private Long clientId;
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
