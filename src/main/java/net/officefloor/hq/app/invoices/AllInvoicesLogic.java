package net.officefloor.hq.app.invoices;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/invoices — list every invoice across all projects, with its project name and stage,
 * served a PAGE at a time. Optionally narrowed to a single lifecycle stage via the {@code status}
 * query parameter. The 1-based {@code page} and the {@code size} query parameters choose the page;
 * both are optional and default to the first page of ten.
 */
public class AllInvoicesLogic {

    private static final int DEFAULT_SIZE = 10;

    public void service(@HttpQueryParameter("status") String status,
            @HttpQueryParameter("page") String page, @HttpQueryParameter("size") String size,
            InvoiceService invoices, ObjectResponse<AllInvoicesPageView> response) {
        response.send(invoices.listAllPaged(status, parse(page, 1), parse(size, DEFAULT_SIZE)));
    }

    private static int parse(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
