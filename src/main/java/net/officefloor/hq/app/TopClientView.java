package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * One row of the home dashboard's "top clients" ranking: a client's id and name paired with how much
 * they owe — the sum, across every one of that client's invoices (gathered from all of their
 * projects), of what is still due on each (the invoice's discounted total minus everything paid
 * against it), the same "money owed" figure {@link ClientOutstandingLogic} uses.
 */
public class TopClientView {

    private final Long id;
    private final String name;
    private final BigDecimal owed;

    public TopClientView(Long id, String name, BigDecimal owed) {
        this.id = id;
        this.name = name;
        this.owed = owed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getOwed() {
        return owed;
    }
}
