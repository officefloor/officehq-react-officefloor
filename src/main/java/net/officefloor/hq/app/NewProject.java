package net.officefloor.hq.app;

/** Request body for creating a project (POST /api/projects). */
public class NewProject {

    private String name;
    private long clientId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }
}
