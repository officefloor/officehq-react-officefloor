package net.officefloor.hq.app.client;

import java.math.BigDecimal;
import java.util.List;

/**
 * One job (project) on a client's statement: the invoices raised for that job gathered together,
 * with the subtotal still owed across them (the sum of what is due on each of the job's invoices).
 */
public class StatementProjectView {

    private final Long projectId;
    private final List<StatementInvoiceView> invoices;
    private final BigDecimal subtotal;

    public StatementProjectView(Long projectId, List<StatementInvoiceView> invoices,
            BigDecimal subtotal) {
        this.projectId = projectId;
        this.invoices = invoices;
        this.subtotal = subtotal;
    }

    public Long getProjectId() {
        return projectId;
    }

    public List<StatementInvoiceView> getInvoices() {
        return invoices;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
