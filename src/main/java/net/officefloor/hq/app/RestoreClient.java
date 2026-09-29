package net.officefloor.hq.app;

/** Request body for restoring an archived client (POST /api/clients/restore). */
public class RestoreClient {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
