package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;

/** One row of the home screen's "top clients" list: a client and how much they owe (discounted SENT total). */
public class TopClientView {

    private final long id;
    private final String name;
    private final BigDecimal owed;

    public TopClientView(long id, String name, BigDecimal owed) {
        this.id = id;
        this.name = name;
        this.owed = owed;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getOwed() {
        return owed;
    }
}
