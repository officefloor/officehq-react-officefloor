package net.officefloor.hq.app.invoice;

import java.util.List;

/**
 * One page of the all-invoices list: the invoices on this page plus enough to drive the next/prev
 * controls (the 1-based page number and whether a page exists on either side).
 */
public class InvoicePageView {

    private final List<AllInvoiceView> items;
    private final int page;
    private final boolean hasPrev;
    private final boolean hasNext;

    public InvoicePageView(List<AllInvoiceView> items, int page, boolean hasPrev, boolean hasNext) {
        this.items = items;
        this.page = page;
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
    }

    public List<AllInvoiceView> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
