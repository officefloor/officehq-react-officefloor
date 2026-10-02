package net.officefloor.hq.app;

import java.math.BigDecimal;

/** An invoice as the UI shows it: the invoice's id, its project's id, the billed amount, and its payment status. */
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
