package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Read-only aggregate queries backing the home dashboard (counts + outstanding total). */
@Repository
public class DashboardRepository {

    private final JdbcTemplate jdbc;

    public DashboardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardSummary summary() {
        long clients = jdbc.queryForObject("SELECT COUNT(*) FROM clients", Long.class);
        long projects = jdbc.queryForObject("SELECT COUNT(*) FROM projects", Long.class);
        // Money still owed = for every issued invoice (stored status SENT), its discounted amount
        // (the sum of its line items qty * unit_price, with the invoice's discount_pct taken off)
        // minus whatever has been paid against it. Amount is derived from line items (V13) and the
        // discount from discount_pct (V29), so what is owed reflects the discount everywhere. DRAFT
        // invoices are not yet issued and VOID invoices were cancelled, so both are excluded (their
        // stored status is not SENT). COALESCE keeps the total at 0.00 (never null) when nothing is
        // outstanding.
        BigDecimal outstanding = jdbc.queryForObject(
                "SELECT COALESCE(SUM("
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li"
                        + " WHERE li.invoice_id = i.id), 0) * (100 - i.discount_pct) / 100"
                        + " - COALESCE((SELECT SUM(pm.amount) FROM payments pm"
                        + " WHERE pm.invoice_id = i.id), 0)), 0)"
                        + " FROM invoices i WHERE i.status = 'SENT'",
                BigDecimal.class);
        // Overdue = invoices that have been SENT and whose due date has already passed relative to
        // the dashboard's reference date. That date is seeded (dashboard_settings.as_of) so the
        // count is deterministic under test; when unset we fall back to the current date. DRAFT
        // invoices are not yet issued so they are never overdue.
        LocalDate asOf = jdbc.query(
                "SELECT as_of FROM dashboard_settings ORDER BY id LIMIT 1",
                rs -> rs.next() && rs.getDate("as_of") != null
                        ? rs.getDate("as_of").toLocalDate() : null);
        if (asOf == null) {
            asOf = LocalDate.now();
        }
        long overdue = jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoices"
                        + " WHERE status = 'SENT' AND due_date IS NOT NULL AND due_date < ?",
                Long.class, Date.valueOf(asOf));
        return new DashboardSummary(clients, projects, outstanding, overdue);
    }

    // What is still owed on an invoice, worked out the same way "due" is everywhere else
    // (ClientOutstandingRepository / InvoiceRepository): its line-item total (qty * unit_price), less
    // the discount (V29), plus tax (V30), minus what has been paid. DRAFT invoices (not yet issued)
    // and VOID invoices (cancelled) do not count toward money owed (V24).
    private static final String DUE =
            "(COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li"
                    + " WHERE li.invoice_id = i.id), 0)"
                    + " * (100 - i.discount_pct) / 100 * (100 + i.tax_pct) / 100"
                    + " - COALESCE((SELECT SUM(pm.amount) FROM payments pm"
                    + " WHERE pm.invoice_id = i.id), 0))";

    /**
     * The home screen's top clients ranked by how much they still owe (highest first), capped at
     * {@code limit}. Non-archived clients only; a client with no owed invoices totals 0.00. Ties break
     * by id so the order is deterministic under test.
     */
    public List<TopClient> topClients(int limit) {
        return jdbc.query(
                "SELECT c.id AS client_id, c.name AS name,"
                        + " COALESCE(SUM(" + DUE + "), 0) AS outstanding"
                        + " FROM clients c"
                        + " LEFT JOIN projects p ON p.client_id = c.id"
                        + " LEFT JOIN invoices i ON i.project_id = p.id"
                        + " AND i.status NOT IN ('DRAFT', 'VOID')"
                        + " WHERE c.archived = FALSE"
                        + " GROUP BY c.id, c.name"
                        + " ORDER BY outstanding DESC, c.id ASC"
                        + " LIMIT ?",
                (rs, n) -> new TopClient(
                        rs.getLong("client_id"), rs.getString("name"),
                        rs.getBigDecimal("outstanding")),
                limit);
    }
}
