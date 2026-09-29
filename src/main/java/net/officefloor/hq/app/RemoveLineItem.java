package net.officefloor.hq.app;

/** Request body for removing a line item from an invoice (POST /api/lineitems/remove). */
public class RemoveLineItem {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
