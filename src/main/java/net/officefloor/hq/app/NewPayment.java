package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * Request body for recording a payment against an invoice: how much the client paid (amount) and the
 * date they paid it (date, ISO {@code yyyy-MM-dd}).
 */
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
