package net.officefloor.hq.app.client;

import java.math.BigDecimal;

/**
 * A client as the clients list shows it: name + email, plus how much they still owe (the sum of what
 * is outstanding across every invoice raised for them). The list can be sorted by name or by this
 * outstanding amount.
 */
public class ClientListView {

    private final long id;
    private final String name;
    private final String email;
    private final BigDecimal outstanding;

    public ClientListView(long id, String name, String email, BigDecimal outstanding) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.outstanding = outstanding;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }
}
