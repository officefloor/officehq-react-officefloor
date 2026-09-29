package net.officefloor.hq.app;

/** Request body for writing a note on an invoice (POST /api/invoices/notes). */
public class NewInvoiceNote {

    private long invoiceId;
    private String text;

    public long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
