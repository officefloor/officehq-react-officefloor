package net.officefloor.hq.app;

/** Request body for writing a note on a project (POST /api/projects/notes). */
public class NewNote {

    private long projectId;
    private String text;

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
