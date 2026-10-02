package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpObject;

/** JSON request body for creating a contact. {@link HttpObject} loads it from the request entity. */
@HttpObject
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
