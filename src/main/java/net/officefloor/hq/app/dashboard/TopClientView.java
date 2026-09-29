package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;

/**
 * One row of the home screen's "top clients" list: a client and how much they still owe. The list
 * is the handful of clients that owe the most, so it carries just the name to show and the
 * outstanding amount to rank and display by.
 */
public class TopClientView {

    private final long id;
    private final String name;
    private final String currency;
    private final BigDecimal outstanding;

    public TopClientView(long id, String name, String currency, BigDecimal outstanding) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.outstanding = outstanding;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }
}
