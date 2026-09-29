package net.officefloor.hq.app;

import java.math.BigDecimal;

/** Request body for creating an invoice (POST /api/invoices). */
public class NewInvoice {

    private long projectId;
    private BigDecimal amount;

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
