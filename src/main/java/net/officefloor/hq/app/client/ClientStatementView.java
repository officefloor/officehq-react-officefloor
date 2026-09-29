package net.officefloor.hq.app.client;

import java.math.BigDecimal;
import java.util.List;

/**
 * A client's statement: all of their invoices gathered in one place — both as a flat list and
 * grouped under each job (project) with that job's subtotal — plus the total still owed across
 * them all (the sum of what is due on each).
 */
public class ClientStatementView {

    private final List<StatementInvoiceView> invoices;
    private final List<StatementProjectView> projects;
    private final BigDecimal outstandingTotal;

    public ClientStatementView(List<StatementInvoiceView> invoices,
            List<StatementProjectView> projects, BigDecimal outstandingTotal) {
        this.invoices = invoices;
        this.projects = projects;
        this.outstandingTotal = outstandingTotal;
    }

    public List<StatementInvoiceView> getInvoices() {
        return invoices;
    }

    public List<StatementProjectView> getProjects() {
        return projects;
    }

    public BigDecimal getOutstandingTotal() {
        return outstandingTotal;
    }
}
