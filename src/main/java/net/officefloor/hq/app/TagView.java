package net.officefloor.hq.app;

/** A tag as the UI shows it: the tag's id and its name. */
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
