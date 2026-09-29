package net.officefloor.hq.app;

/** Request body for cancelling (voiding) a sent invoice (POST /api/invoices/cancel). */
public class CancelInvoice {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
