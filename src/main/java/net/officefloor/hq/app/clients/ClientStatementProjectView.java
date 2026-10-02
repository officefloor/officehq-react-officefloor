package net.officefloor.hq.app.clients;

import java.math.BigDecimal;
import java.util.List;

/**
 * JSON response shape for one job (project) group on a client's statement: the project's id and
 * name, the invoices raised against it, and the subtotal still due across them. The subtotals of a
 * statement's groups add up to the client's overall outstanding total.
 */
public class ClientStatementProjectView {

    private final long id;
    private final String name;
    private final BigDecimal subtotal;
    private final List<ClientStatementInvoiceView> invoices;

    public ClientStatementProjectView(long id, String name, BigDecimal subtotal,
            List<ClientStatementInvoiceView> invoices) {
        this.id = id;
        this.name = name;
        this.subtotal = subtotal;
        this.invoices = invoices;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public List<ClientStatementInvoiceView> getInvoices() {
        return invoices;
    }
}
