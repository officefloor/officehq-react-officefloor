package net.officefloor.hq.app.task;

/** A task as the project detail surfaces it: id, its project, the title, and whether it is done. */
public class TaskView {

    private final Long id;
    private final Long projectId;
    private final String title;
    private final boolean done;

    public TaskView(Long id, Long projectId, String title, boolean done) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.done = done;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDone() {
        return done;
    }
}
