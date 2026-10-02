package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;

/**
 * A client's statement: all of that client's invoices (across every one of their projects) in one
 * place, each carrying how much is still left to pay, plus the outstanding total — the sum of what is
 * still due across those invoices.
 */
public class ClientStatementView {

    private final List<InvoiceView> invoices;
    private final List<StatementGroupView> groups;
    private final BigDecimal outstandingTotal;

    public ClientStatementView(List<InvoiceView> invoices, List<StatementGroupView> groups,
            BigDecimal outstandingTotal) {
        this.invoices = invoices;
        this.groups = groups;
        this.outstandingTotal = outstandingTotal;
    }

    public List<InvoiceView> getInvoices() {
        return invoices;
    }

    public List<StatementGroupView> getGroups() {
        return groups;
    }

    public BigDecimal getOutstandingTotal() {
        return outstandingTotal;
    }
}
