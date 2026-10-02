package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;

/**
 * JSON response shape for an invoice (what the UI renders): id, owning project, money amount,
 * payment status (UNPAID until marked paid), the two dates it carries — when it was issued
 * (sent out) and when payment is due (ISO {@code yyyy-MM-dd} strings) — the amount still
 * left to pay (the money amount minus any payments recorded against it), and the percentage
 * discount taken off the subtotal (0 when there is none).
 */
public class InvoiceView {

    private final long id;
    private final long projectId;
    private final BigDecimal amount;
    private final String status;
    private final String issuedDate;
    private final String dueDate;
    private final BigDecimal dueAmount;
    private final BigDecimal discountPct;
    private final BigDecimal taxPct;
    // The currency the invoice's money is shown in — the owning project's client's currency.
    private final String currency;

    public InvoiceView(long id, long projectId, BigDecimal amount, String status,
            String issuedDate, String dueDate, BigDecimal dueAmount, BigDecimal discountPct,
            BigDecimal taxPct, String currency) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.status = status;
        this.issuedDate = issuedDate;
        this.dueDate = dueDate;
        this.dueAmount = dueAmount;
        this.discountPct = discountPct;
        this.taxPct = taxPct;
        this.currency = currency == null ? "USD" : currency;
    }

    public String getCurrency() {
        return currency;
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

    public BigDecimal getDueAmount() {
        return dueAmount;
    }

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public BigDecimal getTaxPct() {
        return taxPct;
    }
}
