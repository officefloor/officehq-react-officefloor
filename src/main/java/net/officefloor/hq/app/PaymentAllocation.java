package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * One share of a split payment: how much of a lump sum is applied to a single invoice. Part of the
 * {@link NewSplitPayment} request body (POST /api/payments/split).
 */
public class PaymentAllocation {

    private long invoiceId;
    private BigDecimal amount;

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
}
