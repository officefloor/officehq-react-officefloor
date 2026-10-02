package net.officefloor.hq.app.clients;

import java.math.BigDecimal;
import java.util.List;

/**
 * JSON response shape for a client's statement: all of the client's invoices gathered in one place,
 * plus the outstanding total — the sum of what is still due across those invoices.
 */
public class ClientStatementView {

    private final List<ClientStatementInvoiceView> invoices;
    private final BigDecimal outstandingTotal;

    public ClientStatementView(List<ClientStatementInvoiceView> invoices,
            BigDecimal outstandingTotal) {
        this.invoices = invoices;
        this.outstandingTotal = outstandingTotal;
    }

    public List<ClientStatementInvoiceView> getInvoices() {
        return invoices;
    }

    public BigDecimal getOutstandingTotal() {
        return outstandingTotal;
    }
}
