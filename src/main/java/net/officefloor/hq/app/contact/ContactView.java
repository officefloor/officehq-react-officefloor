package net.officefloor.hq.app.contact;

/** A contact as the client's page surfaces it: id, its client, name, email and role. */
public class ContactView {

    private final Long id;
    private final Long clientId;
    private final String name;
    private final String email;
    private final String role;

    public ContactView(Long id, Long clientId, String name, String email, String role) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
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
}
