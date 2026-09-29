package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** An invoice as the project detail surfaces it: id, its project, the amount, and payment status. */
public class InvoiceView {

    private final Long id;
    private final Long projectId;
    private final BigDecimal amount;
    private final String status;

    public InvoiceView(Long id, Long projectId, BigDecimal amount, String status) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
