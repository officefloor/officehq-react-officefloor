package net.officefloor.hq.app.clients;

import java.math.BigDecimal;

/** JSON response shape for a client (what the UI renders). */
public class ClientView {

    private final long id;
    private final String name;
    private final String email;
    // How much the client still owes across all their invoices (amount due). The list can be sorted
    // by this; create/update/archive responses don't carry it, so they default to zero.
    private final BigDecimal outstanding;

    public ClientView(long id, String name, String email) {
        this(id, name, email, BigDecimal.ZERO);
    }

    public ClientView(long id, String name, String email, BigDecimal outstanding) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.outstanding = outstanding == null ? BigDecimal.ZERO : outstanding;
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
