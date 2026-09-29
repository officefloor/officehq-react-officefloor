package net.officefloor.hq.app;

/** Request body for marking an invoice paid (POST /api/invoices/pay). */
public class PayInvoice {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
