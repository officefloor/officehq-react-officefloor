package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** Request body for creating an invoice: the project it is for and its amount. */
public class NewInvoice {

    private Long projectId;

    private BigDecimal amount;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
