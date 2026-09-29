package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for splitting one lump payment across several invoices (POST /api/payments/split).
 * The client pays a single {@code amount} on a single {@code date}; {@code allocations} says how much
 * of it lands on each invoice. One {@link Payment} row is recorded per allocation, so each invoice's
 * balance reflects its share.
 */
public class NewSplitPayment {

    private BigDecimal amount;
    private String date;
    private List<PaymentAllocation> allocations;

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

    public List<PaymentAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<PaymentAllocation> allocations) {
        this.allocations = allocations;
    }
}
