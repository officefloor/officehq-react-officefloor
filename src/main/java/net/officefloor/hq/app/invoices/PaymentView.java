package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/**
 * JSON response shape for a payment (what the UI renders): id, owning invoice, how much was paid and
 * the date it was paid (ISO {@code yyyy-MM-dd}).
 */
public class PaymentView {

    private final long id;
    private final long invoiceId;
    private final BigDecimal amount;
    private final String date;

    public PaymentView(long id, long invoiceId, BigDecimal amount, String date) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}
