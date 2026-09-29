package net.officefloor.hq.app.invoice;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * GET /api/all-invoices — every invoice across all projects, each with its project name and stage,
 * served a page at a time so a large list stays manageable. An optional {@code status} query
 * parameter narrows the list to a single stage; {@code ALL} (or a blank value) leaves it unfiltered.
 * An optional 1-based {@code page} parameter selects the page (defaulting to 1); each page holds
 * {@link #PAGE_SIZE} invoices, and the response reports whether a page exists on either side so the
 * next/prev controls know when to stop.
 */
public class ListAllInvoices {

    /** How many invoices a single page shows. */
    static final int PAGE_SIZE = 10;

    public void service(@HttpQueryParameter("status") String status,
            @HttpQueryParameter("page") String page, JdbcTemplate jdbc,
            ObjectResponse<InvoicePageView> response) {
        RowMapper<AllInvoiceView> mapper = (rs, i) -> new AllInvoiceView(rs.getLong("id"),
                rs.getLong("project_id"), rs.getString("project_name"), rs.getBigDecimal("amount"),
                rs.getString("status"));
        boolean all = status == null || status.isBlank() || "ALL".equalsIgnoreCase(status);
        int pageNumber = parsePage(page);
        int offset = (pageNumber - 1) * PAGE_SIZE;

        String from = "FROM invoices i JOIN projects p ON p.id = i.project_id "
                + (all ? "" : "WHERE i.status = ? ");
        Object[] filterArgs = all ? new Object[0] : new Object[] { status };

        long total = jdbc.queryForObject("SELECT COUNT(*) " + from, Long.class, filterArgs);

        String select = "SELECT i.id, i.project_id, p.name AS project_name, i.status, "
                + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                + "WHERE li.invoice_id = i.id), 0) * (100 - i.discount_pct) / 100 "
                + "* (100 + i.tax_pct) / 100 AS amount ";
        Object[] pageArgs = append(filterArgs, PAGE_SIZE, offset);
        List<AllInvoiceView> items = jdbc.query(
                select + from + "ORDER BY i.id LIMIT ? OFFSET ?", mapper, pageArgs);

        boolean hasPrev = pageNumber > 1;
        boolean hasNext = (long) offset + items.size() < total;
        response.send(new InvoicePageView(items, pageNumber, hasPrev, hasNext));
    }

    /** Parse the 1-based page parameter, defaulting to and flooring at page 1. */
    private static int parsePage(String page) {
        if (page == null || page.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(page.trim()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static Object[] append(Object[] base, Object... extra) {
        Object[] combined = new Object[base.length + extra.length];
        System.arraycopy(base, 0, combined, 0, base.length);
        System.arraycopy(extra, 0, combined, base.length, extra.length);
        return combined;
    }
}
