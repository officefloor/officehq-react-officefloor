package net.officefloor.hq.app.clients;

/** JSON response shape for a project a client owns (what the client detail view renders). */
public class ClientProjectView {

    private final long id;
    private final String name;

    public ClientProjectView(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
