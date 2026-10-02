package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpObject;

/** JSON request body for creating a client. {@link HttpObject} loads it from the request entity. */
@HttpObject
public class NewClient {

    private String name;
    private String email;

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
}
