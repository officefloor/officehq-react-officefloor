package net.officefloor.hq.app.notes;

import net.officefloor.web.HttpObject;

/** JSON request body for writing a note. {@link HttpObject} loads it from the entity. */
@HttpObject
public class NewNote {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
