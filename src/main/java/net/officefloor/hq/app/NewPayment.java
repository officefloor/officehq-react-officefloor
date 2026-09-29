package net.officefloor.hq.app;

import java.math.BigDecimal;

/** Request body for recording a payment against an invoice (POST /api/payments). */
public class NewPayment {

    private long invoiceId;
    private BigDecimal amount;
    private String date;

    public long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
