package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;

/**
 * One job's slice of a client's statement: the project (job) the invoices belong to, those invoices,
 * and the subtotal still due across them. Grouping the statement by job lets the client read what is
 * owed per job; the per-job subtotals sum to the statement's overall outstanding total.
 */
public class StatementGroupView {

    private final Long projectId;
    private final String projectName;
    private final List<InvoiceView> invoices;
    private final BigDecimal subtotal;

    public StatementGroupView(Long projectId, String projectName, List<InvoiceView> invoices,
            BigDecimal subtotal) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.invoices = invoices;
        this.subtotal = subtotal;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<InvoiceView> getInvoices() {
        return invoices;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
