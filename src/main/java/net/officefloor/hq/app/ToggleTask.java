package net.officefloor.hq.app;

/** Request body for ticking a task off / on (POST /api/tasks/toggle). */
public class ToggleTask {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
