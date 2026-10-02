package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/**
 * JSON response shape for the cross-project "all invoices" list: the invoice id, the NAME of the
 * project it was raised against (the UI shows names, not ids), the money amount, and the lifecycle
 * stage (DRAFT -> SENT -> PAID).
 */
public class AllInvoiceView {

    private final long id;
    private final String projectName;
    private final BigDecimal amount;
    private final String status;

    public AllInvoiceView(long id, String projectName, BigDecimal amount, String status) {
        this.id = id;
        this.projectName = projectName;
        this.amount = amount;
        this.status = status;
    }

    public long getId() {
        return id;
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
