package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * How much one client owes: their id paired with their outstanding total — the sum, across every one
 * of that client's invoices (gathered from all of their projects), of what is still due on each (the
 * invoice's discounted total minus everything paid against it). Lets the clients list be ordered by
 * how much each client owes without the list page re-deriving the figure per client.
 */
public class ClientOutstandingView {

    private final Long id;
    private final BigDecimal outstanding;

    public ClientOutstandingView(Long id, BigDecimal outstanding) {
        this.id = id;
        this.outstanding = outstanding;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }
}
