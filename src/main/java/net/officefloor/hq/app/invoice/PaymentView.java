package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** A payment as the invoice detail surfaces it: how much was paid and the date (ISO yyyy-MM-dd). */
public class PaymentView {

    private final Long id;
    private final Long invoiceId;
    private final BigDecimal amount;
    private final String date;

    public PaymentView(Long id, Long invoiceId, BigDecimal amount, String date) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }
}
