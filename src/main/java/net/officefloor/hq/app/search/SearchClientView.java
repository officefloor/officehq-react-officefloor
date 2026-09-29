package net.officefloor.hq.app.search;

/** A client that matched a global search — the id, name and email the search results surface. */
public class SearchClientView {

    private final Long id;
    private final String name;
    private final String email;

    public SearchClientView(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
