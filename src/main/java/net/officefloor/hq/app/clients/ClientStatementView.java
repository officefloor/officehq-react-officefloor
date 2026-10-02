package net.officefloor.hq.app.clients;

import java.math.BigDecimal;
import java.util.List;

/**
 * JSON response shape for a client's statement: all of the client's invoices gathered in one place
 * (flat), the same invoices grouped under the job (project) they were raised against with a subtotal
 * per job, plus the outstanding total — the sum of what is still due across those invoices. The job
 * subtotals add up to the outstanding total.
 */
public class ClientStatementView {

    private final List<ClientStatementInvoiceView> invoices;
    private final List<ClientStatementProjectView> projects;
    private final BigDecimal outstandingTotal;

    public ClientStatementView(List<ClientStatementInvoiceView> invoices,
            List<ClientStatementProjectView> projects, BigDecimal outstandingTotal) {
        this.invoices = invoices;
        this.projects = projects;
        this.outstandingTotal = outstandingTotal;
    }

    public List<ClientStatementInvoiceView> getInvoices() {
        return invoices;
    }

    public List<ClientStatementProjectView> getProjects() {
        return projects;
    }

    public BigDecimal getOutstandingTotal() {
        return outstandingTotal;
    }
}
