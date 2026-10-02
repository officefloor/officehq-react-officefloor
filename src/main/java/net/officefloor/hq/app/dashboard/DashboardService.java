package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;
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
        BigDecimal outstanding = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM invoices WHERE status = 'SENT'",
                BigDecimal.class);
        // A SENT invoice is overdue once its due date has passed relative to the dashboard's fixed
        // reference date (as_of); with no reference seeded it falls back to the current date.
        long overdue = jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE status = 'SENT' AND due_date < "
                        + "COALESCE((SELECT as_of FROM dashboard_settings WHERE id = 1), CURRENT_DATE)",
                Long.class);
        return new DashboardView(clients, projects, outstanding, overdue);
    }
}
