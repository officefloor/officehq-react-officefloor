package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/** JSON response shape for an invoice (what the UI renders): id, owning project, and money amount. */
public class InvoiceView {

    private final long id;
    private final long projectId;
    private final BigDecimal amount;

    public InvoiceView(long id, long projectId, BigDecimal amount) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
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
}
