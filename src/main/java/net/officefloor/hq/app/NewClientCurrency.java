package net.officefloor.hq.app;

/** Request body for setting a client's currency: just the chosen currency code (e.g. USD, EUR). */
public class NewClientCurrency {

    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
