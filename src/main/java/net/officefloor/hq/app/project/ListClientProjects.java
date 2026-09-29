package net.officefloor.hq.app.project;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/clients/{id}/projects — every project owned by one client, oldest first. */
public class ListClientProjects {

    public void service(@HttpPathParameter("id") String id, JdbcTemplate jdbc,
            ObjectResponse<List<ProjectView>> response) {
        List<ProjectView> projects = jdbc.query(
                "SELECT p.id, p.name, p.client_id, p.archived, p.status, c.name AS client_name "
                        + "FROM projects p JOIN clients c ON c.id = p.client_id "
                        + "WHERE p.client_id = ? AND p.archived = FALSE ORDER BY p.id",
                (rs, i) -> new ProjectView(rs.getLong("id"), rs.getString("name"),
                        rs.getLong("client_id"), rs.getString("client_name"),
                        rs.getBoolean("archived"), rs.getString("status")),
                Long.valueOf(id));
        response.send(projects);
    }
}
