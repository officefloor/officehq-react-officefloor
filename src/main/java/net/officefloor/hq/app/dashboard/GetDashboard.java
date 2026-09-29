package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;
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
        BigDecimal outstanding = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM invoices WHERE status = 'SENT'",
                BigDecimal.class);
        long overdue = jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoices WHERE status = 'SENT' AND due_date < "
                        + "COALESCE((SELECT as_of FROM dashboard_clock WHERE id = 1), CURRENT_DATE)",
                Long.class);
        response.send(new DashboardView(clients, projects, outstanding, overdue));
    }
}
