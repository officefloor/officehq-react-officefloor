package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for recording a lump payment a client made and splitting it across their invoices: how
 * much the client paid in total (amount), the date they paid it (date, ISO {@code yyyy-MM-dd}) and how
 * that lump is allocated — a share (amount) against each invoice (invoiceId). The allocated shares
 * must add up to the lump amount.
 */
public class NewClientPayment {

    private BigDecimal amount;

    private String date;

    private List<Allocation> allocations;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations;
    }

    /** One share of the lump: how much (amount) goes against which invoice (invoiceId). */
    public static class Allocation {

        private Long invoiceId;

        private BigDecimal amount;

        public Long getInvoiceId() {
            return invoiceId;
        }

        public void setInvoiceId(Long invoiceId) {
            this.invoiceId = invoiceId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
