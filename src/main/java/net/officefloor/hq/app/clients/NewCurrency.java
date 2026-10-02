package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpObject;

/**
 * JSON request body for setting the currency a client is paid in. {@link HttpObject} loads it from
 * the request entity.
 */
@HttpObject
public class NewCurrency {

    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
