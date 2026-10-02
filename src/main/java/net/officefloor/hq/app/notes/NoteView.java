package net.officefloor.hq.app.notes;

/** JSON response shape for a note: its id, the written text, and when it was written. */
public class NoteView {

    private final long id;
    private final String text;
    private final String at;

    public NoteView(long id, String text, String at) {
        this.id = id;
        this.text = text;
        this.at = at;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getAt() {
        return at;
    }
}
