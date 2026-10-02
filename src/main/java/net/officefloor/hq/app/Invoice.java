package net.officefloor.hq.app;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** An invoice billed against a project: the owning project's id and a monetary amount. Maps to {@code invoice} (V4). */
@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    private BigDecimal amount;

    private String status;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "discount_pct")
    private int discountPct;

    @Column(name = "tax_pct")
    private int taxPct;

    public Invoice() {
    }

    public Invoice(Long projectId, BigDecimal amount) {
        this.projectId = projectId;
        this.amount = amount;
        this.status = "DRAFT";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getDiscountPct() {
        return discountPct;
    }

    public void setDiscountPct(int discountPct) {
        this.discountPct = discountPct;
    }

    public int getTaxPct() {
        return taxPct;
    }

    public void setTaxPct(int taxPct) {
        this.taxPct = taxPct;
    }

    /**
     * The invoice's final total — the stored {@code amount} (the undiscounted subtotal of its line
     * items, per V30) with the owner's percentage discount taken off. This is what is actually owed,
     * so it is what every "money owed" read should total. Division by 100 is exact, so no rounding.
     */
    public BigDecimal getDiscountedAmount() {
        if (amount == null) {
            return null;
        }
        BigDecimal discount = amount.multiply(BigDecimal.valueOf(discountPct))
                .divide(BigDecimal.valueOf(100));
        return amount.subtract(discount);
    }
}
