package net.officefloor.hq.app.client;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/clients/{id}/summary — one client's at-a-glance counts: how many projects and how many
 * contacts are kept for them.
 */
public class GetClientSummary {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<ClientSummaryView> response) {
        long projects = jdbc.queryForObject(
                "SELECT COUNT(*) FROM projects WHERE client_id = ?", Long.class,
                Long.valueOf(id));
        long contacts = jdbc.queryForObject(
                "SELECT COUNT(*) FROM contacts WHERE client_id = ?", Long.class,
                Long.valueOf(id));
        response.send(new ClientSummaryView(projects, contacts));
    }
}
