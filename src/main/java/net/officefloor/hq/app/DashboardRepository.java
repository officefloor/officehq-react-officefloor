package net.officefloor.hq.app;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
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
        // Money still owed = for every issued invoice (stored status SENT), the sum of its
        // line-item amount (qty * unit_price) minus whatever has been paid against it. Amount is
        // derived from line items (V13), so the stored amount column is not used. DRAFT invoices are
        // not yet issued and VOID invoices were cancelled, so both are excluded (their stored status
        // is not SENT). COALESCE keeps the total at 0.00 (never null) when nothing is outstanding.
        BigDecimal outstanding = jdbc.queryForObject(
                "SELECT COALESCE(SUM("
                        + "COALESCE((SELECT SUM(li.qty * li.unit_price) FROM line_items li"
                        + " WHERE li.invoice_id = i.id), 0)"
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
}
