package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;

/**
 * One currency's slice of the home screen's outstanding money: the currency and the total owed in
 * it. Totals are kept separate per currency and never added together across currencies.
 */
public class CurrencyTotalView {

    private final String currency;
    private final BigDecimal amount;

    public CurrencyTotalView(String currency, BigDecimal amount) {
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
