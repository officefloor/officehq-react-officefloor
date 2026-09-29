package net.officefloor.hq.app.client;

import java.math.BigDecimal;

/** One line of a client's statement: an invoice of theirs and what is still owed on it. */
public class StatementInvoiceView {

    private final Long id;
    private final Long projectId;
    private final BigDecimal amountDue;

    public StatementInvoiceView(Long id, Long projectId, BigDecimal amountDue) {
        this.id = id;
        this.projectId = projectId;
        this.amountDue = amountDue;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }
}
