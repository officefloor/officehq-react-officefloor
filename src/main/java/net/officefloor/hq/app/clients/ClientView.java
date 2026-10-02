package net.officefloor.hq.app.clients;

/** JSON response shape for a client (what the UI renders). */
public class ClientView {

    private final long id;
    private final String name;
    private final String email;

    public ClientView(long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
