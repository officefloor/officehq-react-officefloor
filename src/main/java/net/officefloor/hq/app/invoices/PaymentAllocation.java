package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/**
 * One invoice's share of a split lump payment: which invoice the share goes to and how much of the
 * lump sum is applied to it. Part of {@link SplitPayment}; populated by Jackson from the request body.
 */
public class PaymentAllocation {

    private Long invoiceId;
    private BigDecimal amount;

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
