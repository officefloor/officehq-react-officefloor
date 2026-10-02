package net.officefloor.hq.app;

/** Request body for writing a note: the note text. */
public class NewNote {

    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
