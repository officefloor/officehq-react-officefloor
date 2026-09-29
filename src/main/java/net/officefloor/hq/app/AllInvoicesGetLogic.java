package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices/all?page=&lt;n&gt;&amp;status=&lt;stage&gt; — one page of every invoice across all
 * projects (10 per page), with its project name and stage. The list has grown large, so it is
 * served a page at a time; the response carries the total so the UI can offer next/previous.
 */
public class AllInvoicesGetLogic {

    static final int PAGE_SIZE = 10;

    public void service(@HttpQueryParameter("page") String page,
            @HttpQueryParameter("status") String status, InvoiceRepository repository,
            ObjectResponse<InvoicePage> response) {
        int p = parsePage(page);
        String filter = status == null || status.isBlank() ? null : status.trim();
        long total = repository.countAll(filter);
        List<InvoiceListing> invoices =
                repository.findPage(filter, PAGE_SIZE, (p - 1) * PAGE_SIZE);
        response.send(new InvoicePage(invoices, total, p, PAGE_SIZE));
    }

    /** A page number is 1-based; anything missing or unparseable falls back to the first page. */
    private static int parsePage(String page) {
        if (page == null || page.isBlank()) {
            return 1;
        }
        try {
            int n = Integer.parseInt(page.trim());
            return n < 1 ? 1 : n;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
