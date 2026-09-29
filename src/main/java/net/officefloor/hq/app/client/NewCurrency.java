package net.officefloor.hq.app.client;

/** Request body for setting a client's currency (the ISO code they are paid in, e.g. USD, EUR). */
public class NewCurrency {

    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
