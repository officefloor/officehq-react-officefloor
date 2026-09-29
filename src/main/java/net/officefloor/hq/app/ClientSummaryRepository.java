package net.officefloor.hq.app;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Read-only count queries backing a client's detail view (projects + contacts for one client). */
@Repository
public class ClientSummaryRepository {

    private final JdbcTemplate jdbc;

    public ClientSummaryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ClientSummary forClient(long clientId) {
        long projects = jdbc.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE client_id = ?", Long.class, clientId);
        long contacts = jdbc.queryForObject(
                "SELECT COUNT(*) FROM contacts WHERE client_id = ?", Long.class, clientId);
        return new ClientSummary(projects, contacts);
    }
}
