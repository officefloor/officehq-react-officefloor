package net.officefloor.hq.app;

/** Request body for sending a draft invoice (POST /api/invoices/send). */
public class SendInvoice {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
