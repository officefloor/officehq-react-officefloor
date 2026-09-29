package net.officefloor.hq.app.note;

/** A note as the UI surfaces it: id, its target, the text, and when it was written. */
public class NoteView {

    private final Long id;
    private final String targetType;
    private final Long targetId;
    private final String text;
    private final String at;

    public NoteView(Long id, String targetType, Long targetId, String text, String at) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.text = text;
        this.at = at;
    }

    public Long getId() {
        return id;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getText() {
        return text;
    }

    public String getAt() {
        return at;
    }
}
