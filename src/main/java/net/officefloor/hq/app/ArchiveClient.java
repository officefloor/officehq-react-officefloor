package net.officefloor.hq.app;

/** Request body for archiving a client (POST /api/clients/archive). */
public class ArchiveClient {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
