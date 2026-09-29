package net.officefloor.hq.app.client;

import java.math.BigDecimal;

/**
 * The result of recording a split lump-sum payment: the batch reference tying its shares together,
 * how many invoices were paid into, and the total applied across them.
 */
public class SplitPaymentView {

    private final Long batch;
    private final int count;
    private final BigDecimal total;

    public SplitPaymentView(Long batch, int count, BigDecimal total) {
        this.batch = batch;
        this.count = count;
        this.total = total;
    }

    public Long getBatch() {
        return batch;
    }

    public int getCount() {
        return count;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
