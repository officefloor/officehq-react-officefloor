package net.officefloor.hq.app;

import java.math.BigDecimal;
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
        // Money still owed = sum of UNPAID invoice amounts across all projects. COALESCE keeps the
        // total at 0.00 (never null) when nothing is outstanding.
        BigDecimal outstanding = jdbc.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM invoices WHERE status = 'UNPAID'",
                BigDecimal.class);
        return new DashboardSummary(clients, projects, outstanding);
    }
}
