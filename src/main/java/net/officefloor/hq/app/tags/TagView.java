package net.officefloor.hq.app.tags;

/** JSON response shape for a tag (what the UI renders as a chip): its id and label. */
public class TagView {

    private final long id;
    private final String name;

    public TagView(long id, String name) {
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
