package net.officefloor.hq.app.invoices;

import java.util.List;

/**
 * JSON response shape for one PAGE of the cross-project "all invoices" list. The list is large, so
 * it is served a page at a time: {@code items} holds the rows for the requested page, {@code page}
 * is the 1-based page number served, {@code pageCount} is how many pages exist in total (at least
 * one, so an empty list still reports page 1 of 1), and {@code total} is the full row count.
 */
public class AllInvoicesPageView {

    private final List<AllInvoiceView> items;
    private final int page;
    private final int pageCount;
    private final long total;

    public AllInvoicesPageView(List<AllInvoiceView> items, int page, int pageCount, long total) {
        this.items = items;
        this.page = page;
        this.pageCount = pageCount;
        this.total = total;
    }

    public List<AllInvoiceView> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public long getTotal() {
        return total;
    }
}
