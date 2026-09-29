package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** Request body for recording a payment: how much the client paid and the date they paid it. */
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
