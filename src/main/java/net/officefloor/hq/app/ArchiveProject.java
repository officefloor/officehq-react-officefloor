package net.officefloor.hq.app;

/** Request body for archiving a project (POST /api/projects/archive). */
public class ArchiveProject {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
