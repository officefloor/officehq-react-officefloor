package net.officefloor.hq.app;

import java.math.BigDecimal;

/** An invoice as the UI shows it: the invoice's id, its project's id, and the billed amount. */
public class InvoiceView {

    private final Long id;
    private final Long projectId;
    private final BigDecimal amount;

    public InvoiceView(Long id, Long projectId, BigDecimal amount) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
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
}
