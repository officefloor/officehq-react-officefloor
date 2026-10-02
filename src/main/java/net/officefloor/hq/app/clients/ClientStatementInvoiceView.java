package net.officefloor.hq.app.clients;

import java.math.BigDecimal;

/**
 * JSON response shape for one line of a client's statement: the invoice id, the NAME of the project
 * it was raised against, its money amount, its lifecycle stage, and how much is still due on it
 * (amount minus whatever has been paid against it).
 */
public class ClientStatementInvoiceView {

    private final long id;
    private final String projectName;
    private final BigDecimal amount;
    private final String status;
    private final BigDecimal due;

    public ClientStatementInvoiceView(long id, String projectName, BigDecimal amount, String status,
            BigDecimal due) {
        this.id = id;
        this.projectName = projectName;
        this.amount = amount;
        this.status = status;
        this.due = due;
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

    public BigDecimal getDue() {
        return due;
    }
}
