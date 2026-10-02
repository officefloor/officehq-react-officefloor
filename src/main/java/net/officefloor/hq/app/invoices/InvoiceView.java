package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/**
 * JSON response shape for an invoice (what the UI renders): id, owning project, money amount, and
 * payment status (UNPAID until marked paid).
 */
public class InvoiceView {

    private final long id;
    private final long projectId;
    private final BigDecimal amount;
    private final String status;

    public InvoiceView(long id, long projectId, BigDecimal amount, String status) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getProjectId() {
        return projectId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
