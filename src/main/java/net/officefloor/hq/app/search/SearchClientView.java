package net.officefloor.hq.app.search;

/** JSON response shape for a client matched by the global search. */
public class SearchClientView {

    private final long id;
    private final String name;
    private final String email;

    public SearchClientView(long id, String name, String email) {
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
