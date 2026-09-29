package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/dashboard — the home screen summary: how many clients and projects exist, how much
 * money is still owed, and how many SENT invoices are overdue. Money owed counts only invoices that
 * have actually been SENT (not drafts, and not those already paid) — the sum of every SENT
 * invoice's amount across all projects. An invoice is overdue when it is SENT and its due date is
 * before the dashboard's reference date (the seeded {@code dashboard_clock} as-of date, falling
 * back to the real current date when none is seeded).
 */
public class GetDashboard {

    public void service(JdbcTemplate jdbc, ObjectResponse<DashboardView> response) {
        long clients = jdbc.queryForObject("SELECT COUNT(*) FROM clients", Long.class);
        long projects = jdbc.queryForObject("SELECT COUNT(*) FROM projects", Long.class);
        // Money owed is the total of every SENT invoice, and an invoice's total is the sum of its
        // line items less its discount, with sales tax then added on top (the source of truth
        // everywhere else) — so a cancelled (VOID) invoice, no longer SENT, drops out of what is
        // owed, and both a discount and its tax are reflected in what is owed. The discount and tax
        // are applied per invoice (its own percentages off/on its own subtotal) before the totals
        // are summed. Because clients are paid in different currencies, the totals are kept SEPARATE
        // per currency (grouped by the invoice's client's currency) and never added across them.
        Map<String, BigDecimal> outstandingByCurrency = new LinkedHashMap<>();
        jdbc.query(
                "SELECT inv.currency AS currency, "
                        + "COALESCE(SUM(inv.subtotal * (100 - inv.discount_pct) / 100 "
                        + "* (100 + inv.tax_pct) / 100), 0) AS outstanding FROM ("
                        + "SELECT i.discount_pct AS discount_pct, i.tax_pct AS tax_pct, "
                        + "c.currency AS currency, "
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li "
                        + "WHERE li.invoice_id = i.id), 0) AS subtotal "
                        + "FROM invoices i JOIN projects pr ON pr.id = i.project_id "
                        + "JOIN clients c ON c.id = pr.client_id "
                        + "WHERE i.status = 'SENT') inv "
                        + "GROUP BY inv.currency ORDER BY inv.currency",
                (rs) -> {
                    outstandingByCurrency.put(rs.getString("currency"),
                            rs.getBigDecimal("outstanding"));
                });
        long overdue = jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE status = 'SENT' AND due_date < "
                        + "COALESCE((SELECT as_of FROM dashboard_clock WHERE id = 1), CURRENT_DATE)",
                Long.class);
        response.send(new DashboardView(clients, projects, outstandingByCurrency, overdue));
    }
}
