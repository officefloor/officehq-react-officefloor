package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cross-feature aggregate for the home screen: how many clients and projects the user has, and how
 * much money is still owed (the sum of SENT invoice amounts across every project — drafts have not
 * been billed to the client yet and paid invoices are settled, so only sent-and-unpaid counts). It reads the
 * other features' tables via SQL so the dashboard stays self-contained and does not import their
 * Java types.
 */
@Service
public class DashboardService {

    private final JdbcTemplate jdbc;

    public DashboardService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public DashboardView summary() {
        long clients = jdbc.queryForObject("SELECT COUNT(*) FROM clients", Long.class);
        long projects = jdbc.queryForObject("SELECT COUNT(*) FROM projects", Long.class);
        // What is owed is the discounted total: an invoice's percentage discount comes off its
        // subtotal before it counts toward the outstanding figure, so the home screen shows the same
        // money the client is actually asked to pay (matching the invoice and statement views).
        BigDecimal outstanding = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount * (1 - discount_pct / 100)), 0) FROM invoices WHERE status = 'SENT'",
                BigDecimal.class);
        // A SENT invoice is overdue once its due date has passed relative to the dashboard's fixed
        // reference date (as_of); with no reference seeded it falls back to the current date.
        long overdue = jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE status = 'SENT' AND due_date < "
                        + "COALESCE((SELECT as_of FROM dashboard_settings WHERE id = 1), CURRENT_DATE)",
                Long.class);
        // The top five clients ranked by how much they owe: each client's discounted SENT total
        // across all their projects' invoices, highest first. Only clients that owe something appear
        // (ties broken by id for a stable order).
        List<TopClientView> topClients = jdbc.query(
                "SELECT c.id, c.name, "
                        + "COALESCE(SUM(i.amount * (1 - i.discount_pct / 100)), 0) AS owed "
                        + "FROM clients c "
                        + "JOIN projects p ON p.client_id = c.id "
                        + "JOIN invoices i ON i.project_id = p.id AND i.status = 'SENT' "
                        + "GROUP BY c.id, c.name "
                        + "HAVING SUM(i.amount * (1 - i.discount_pct / 100)) > 0 "
                        + "ORDER BY owed DESC, c.id ASC "
                        + "LIMIT 5",
                (rs, rowNum) -> new TopClientView(
                        rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("owed")));
        return new DashboardView(clients, projects, outstanding, overdue, topClients);
    }
}
