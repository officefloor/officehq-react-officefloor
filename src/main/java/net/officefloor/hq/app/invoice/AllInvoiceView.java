package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** An invoice as the all-invoices list surfaces it: its project's name and its stage (status). */
public class AllInvoiceView {

    private final Long id;
    private final Long projectId;
    private final String projectName;
    private final BigDecimal amount;
    private final String status;

    public AllInvoiceView(Long id, Long projectId, String projectName, BigDecimal amount,
            String status) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.amount = amount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
