package net.officefloor.hq.app.dashboard;

import java.math.BigDecimal;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/dashboard — the home screen summary: how many clients and projects exist, and how much
 * money is still owed (the sum of every UNPAID invoice's amount across all projects).
 */
public class GetDashboard {

    public void service(JdbcTemplate jdbc, ObjectResponse<DashboardView> response) {
        long clients = jdbc.queryForObject("SELECT COUNT(*) FROM clients", Long.class);
        long projects = jdbc.queryForObject("SELECT COUNT(*) FROM projects", Long.class);
        BigDecimal outstanding = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM invoices WHERE status = 'UNPAID'",
                BigDecimal.class);
        response.send(new DashboardView(clients, projects, outstanding));
    }
}
