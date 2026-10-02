package net.officefloor.hq.app;

import java.math.BigDecimal;

/** Request body for creating an invoice: the amount the owner types for a project. */
public class NewInvoice {

    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
