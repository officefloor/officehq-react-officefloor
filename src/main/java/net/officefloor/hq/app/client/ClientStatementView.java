package net.officefloor.hq.app.client;

import java.math.BigDecimal;
import java.util.List;

/**
 * A client's statement: all of their invoices gathered in one place, with the total still owed
 * across them (the sum of what is due on each).
 */
public class ClientStatementView {

    private final List<StatementInvoiceView> invoices;
    private final BigDecimal outstandingTotal;

    public ClientStatementView(List<StatementInvoiceView> invoices, BigDecimal outstandingTotal) {
        this.invoices = invoices;
        this.outstandingTotal = outstandingTotal;
    }

    public List<StatementInvoiceView> getInvoices() {
        return invoices;
    }

    public BigDecimal getOutstandingTotal() {
        return outstandingTotal;
    }
}
