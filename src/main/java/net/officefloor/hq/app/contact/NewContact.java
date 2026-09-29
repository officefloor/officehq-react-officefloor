package net.officefloor.hq.app.contact;

/** Request body for adding a contact to a client: a name, an email and a role. */
public class NewContact {

    private String name;

    private String email;

    private String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
