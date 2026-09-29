package net.officefloor.hq.app;

/** Request body for setting a client's currency (POST /api/clients/currency). */
public class SetClientCurrency {

    private long id;
    private String currency;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
