package net.officefloor.hq.app.note;

/** Request body for writing a note: the target it is about and the note text. */
public class NewNote {

    private String targetType;

    private Long targetId;

    private String text;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
