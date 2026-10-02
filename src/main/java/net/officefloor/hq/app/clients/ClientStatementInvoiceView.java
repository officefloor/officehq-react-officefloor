package net.officefloor.hq.app.clients;

import java.math.BigDecimal;

/**
 * JSON response shape for one line of a client's statement: the invoice id, the id and NAME of the
 * project (job) it was raised against, its money amount, its lifecycle stage, and how much is still
 * due on it (amount minus whatever has been paid against it). The project id lets the statement
 * group invoices under the job they belong to.
 */
public class ClientStatementInvoiceView {

    private final long id;
    private final long projectId;
    private final String projectName;
    private final BigDecimal amount;
    private final String status;
    private final BigDecimal due;

    public ClientStatementInvoiceView(long id, long projectId, String projectName, BigDecimal amount,
            String status, BigDecimal due) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.amount = amount;
        this.status = status;
        this.due = due;
    }

    public long getId() {
        return id;
    }

    public long getProjectId() {
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

    public BigDecimal getDue() {
        return due;
    }
}
