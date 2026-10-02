package net.officefloor.hq.app.invoices;

import java.math.BigDecimal;
import java.util.List;
import net.officefloor.web.HttpObject;

/**
 * JSON request body for recording one lump payment a client made and splitting it across several of
 * their invoices: the lump sum, the date it was paid (ISO {@code yyyy-MM-dd}), and how much of it goes
 * to each invoice. {@link HttpObject} loads it from the request entity.
 */
@HttpObject
public class SplitPayment {

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
