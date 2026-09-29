package net.officefloor.hq.app.tag;

/** A tag (label) as the UI surfaces it: its id and name. */
public class TagView {

    private final Long id;
    private final String name;

    public TagView(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
