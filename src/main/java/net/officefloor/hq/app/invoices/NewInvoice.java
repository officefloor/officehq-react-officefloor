package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;
import net.officefloor.web.HttpObject;

/** JSON request body for creating an invoice. {@link HttpObject} loads it from the request entity. */
@HttpObject
public class NewInvoice {

    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
