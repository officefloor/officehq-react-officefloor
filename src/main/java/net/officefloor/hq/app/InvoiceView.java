package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * An invoice as the UI shows it: the invoice's id, its project's id, the billed amount, how much is
 * still left to pay after payments ({@code due}), its payment status, and the dates it was issued
 * and is due (ISO {@code yyyy-MM-dd}, or null when unset).
 */
public class InvoiceView {

    private final Long id;
    private final Long projectId;
    private final BigDecimal amount;
    private final BigDecimal due;
    private final String status;
    private final String issuedDate;
    private final String dueDate;

    /** Default {@code due} to the full amount, for callers that have no payment information. */
    public InvoiceView(Long id, Long projectId, BigDecimal amount, String status,
            String issuedDate, String dueDate) {
        this(id, projectId, amount, amount, status, issuedDate, dueDate);
    }

    public InvoiceView(Long id, Long projectId, BigDecimal amount, BigDecimal due, String status,
            String issuedDate, String dueDate) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.due = due;
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

    public BigDecimal getDue() {
        return due;
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
