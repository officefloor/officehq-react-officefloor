package net.officefloor.hq.app.clients;

/** JSON response shape for a client's contact (what the client detail view renders). */
public class ClientContactView {

    private final long id;
    private final long clientId;
    private final String name;
    private final String email;
    private final String role;
    private final boolean primary;

    public ClientContactView(long id, long clientId, String name, String email, String role,
            boolean primary) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.primary = primary;
    }

    public long getId() {
        return id;
    }

    public long getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isPrimary() {
        return primary;
    }
}
