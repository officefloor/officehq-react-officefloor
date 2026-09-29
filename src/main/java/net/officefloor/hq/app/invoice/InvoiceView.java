package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** An invoice as the project detail surfaces it: id, its project, the amount, and payment status. */
public class InvoiceView {

    private final Long id;
    private final Long projectId;
    private final BigDecimal amount;
    private final String status;
    private final String issuedDate;
    private final String dueDate;

    public InvoiceView(Long id, Long projectId, BigDecimal amount, String status,
            String issuedDate, String dueDate) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.status = status;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
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

    public String getIssuedDate() {
        return issuedDate;
    }

    public String getDueDate() {
        return dueDate;
    }
}
