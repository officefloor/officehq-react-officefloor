package net.officefloor.hq.app.tasks;

/**
 * JSON response shape for a task (what the UI renders): id, owning project, title, and whether it
 * has been ticked off. The UI shows {@code done} as a status (OPEN when false, DONE when true).
 */
public class TaskView {

    private final long id;
    private final long projectId;
    private final String title;
    private final boolean done;

    public TaskView(long id, long projectId, String title, boolean done) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.done = done;
    }

    public long getId() {
        return id;
    }

    public long getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDone() {
        return done;
    }
}
