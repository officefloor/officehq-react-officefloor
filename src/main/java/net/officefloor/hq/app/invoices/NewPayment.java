package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;
import net.officefloor.web.HttpObject;

/**
 * JSON request body for recording a payment against an invoice: how much was paid and the date it
 * was paid (ISO {@code yyyy-MM-dd}). {@link HttpObject} loads it from the request entity.
 */
@HttpObject
public class NewPayment {

    private BigDecimal amount;
    private String date;

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
