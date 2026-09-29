package net.officefloor.hq.app.invoice;

import java.math.BigDecimal;

/** An invoice as the project detail surfaces it: id, its project, the amount, and payment status. */
public class InvoiceView {

    private final Long id;
    private final Long projectId;
    private final BigDecimal amount;
    private final BigDecimal amountDue;
    private final String status;
    private final String issuedDate;
    private final String dueDate;
    // The percentage discount taken off the subtotal; 0 unless the invoice carries one. Set after
    // construction so the existing callers that don't surface a discount stay untouched.
    private BigDecimal discountPct = BigDecimal.ZERO;
    // The percentage sales tax added on top after the discount; 0 unless the invoice carries one.
    // Set after construction so callers that don't surface a tax stay untouched.
    private BigDecimal taxPct = BigDecimal.ZERO;
    // The currency (ISO code) the invoice's client is paid in, so its money is shown in that
    // currency. Defaults to USD; set after construction so callers that don't surface it stay
    // untouched.
    private String currency = "USD";

    public InvoiceView(Long id, Long projectId, BigDecimal amount, BigDecimal amountDue,
            String status, String issuedDate, String dueDate) {
        this.id = id;
        this.projectId = projectId;
        this.amount = amount;
        this.amountDue = amountDue;
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

    public BigDecimal getAmountDue() {
        return amountDue;
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

    public BigDecimal getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(BigDecimal discountPct) {
        this.discountPct = discountPct;
    }

    public BigDecimal getTaxPct() {
        return taxPct;
    }

    public void setTaxPct(BigDecimal taxPct) {
        this.taxPct = taxPct;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
