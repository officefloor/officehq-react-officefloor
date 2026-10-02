package net.officefloor.hq.app;

import java.math.BigDecimal;

/**
 * A recorded lump client payment as the UI sees it: its id, the owning client's id, the lump amount,
 * the date it was paid (ISO {@code yyyy-MM-dd}) and how many invoices the lump was split across.
 */
public class ClientPaymentView {

    private final Long id;
    private final Long clientId;
    private final BigDecimal amount;
    private final String date;
    private final int allocationCount;

    public ClientPaymentView(Long id, Long clientId, BigDecimal amount, String date,
            int allocationCount) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.date = date;
        this.allocationCount = allocationCount;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public int getAllocationCount() {
        return allocationCount;
    }
}
