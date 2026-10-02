package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/**
 * JSON response shape for an invoice (what the UI renders): id, owning project, money amount,
 * payment status (UNPAID until marked paid), and the two dates it carries — when it was issued
 * (sent out) and when payment is due (ISO {@code yyyy-MM-dd} strings).
 */
public class InvoiceView {

    private final long id;
    private final long projectId;
    private final BigDecimal amount;
    private final String status;
    private final String issuedDate;
    private final String dueDate;

    public InvoiceView(long id, long projectId, BigDecimal amount, String status,
            String issuedDate, String dueDate) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.status = status;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
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
