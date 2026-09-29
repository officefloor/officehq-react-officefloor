package net.officefloor.hq.app.client;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for recording one lump-sum client payment split across several invoices: the lump
 * total, the date it was paid, and how that lump is shared out — one {@link Allocation} per invoice
 * the money is put against.
 */
public class NewSplitPayment {

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

    /** One share of the lump sum: how much of it is applied to which invoice. */
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
