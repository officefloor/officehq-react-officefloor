package net.officefloor.hq.app;

/** Request body for deleting a project (POST /api/projects/delete). */
public class DeleteProject {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
