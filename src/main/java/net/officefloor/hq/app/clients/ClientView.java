package net.officefloor.hq.app.clients;

import java.math.BigDecimal;

/** JSON response shape for a client (what the UI renders). */
public class ClientView {

    private final long id;
    private final String name;
    private final String email;
    // The currency the client is paid in (e.g. USD, EUR). Their money is shown in it everywhere.
    private final String currency;
    // How much the client still owes across all their invoices (amount due), in their currency. The
    // list can be sorted by this; create/update/archive responses don't carry it, so they default to
    // zero.
    private final BigDecimal outstanding;

    public ClientView(long id, String name, String email, String currency) {
        this(id, name, email, currency, BigDecimal.ZERO);
    }

    public ClientView(long id, String name, String email, String currency, BigDecimal outstanding) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.currency = currency == null ? "USD" : currency;
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

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }
}
