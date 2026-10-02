package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * An amount of money paired with the currency it is in. Lets the home dashboard keep its outstanding
 * totals separate per currency — different currencies are never added together (V35).
 */
public class CurrencyAmountView {

    private final String currency;
    private final BigDecimal amount;

    public CurrencyAmountView(String currency, BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
