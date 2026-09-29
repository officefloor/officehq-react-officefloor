package net.officefloor.hq.app.project;

import java.util.List;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/projects — every project with its client's name (cross-entity join). */
public class ListProjects {

    public void service(JdbcTemplate jdbc, ObjectResponse<List<ProjectView>> response) {
        List<ProjectView> projects = jdbc.query(
                "SELECT p.id, p.name, p.client_id, c.name AS client_name "
                        + "FROM projects p JOIN clients c ON c.id = p.client_id "
                        + "ORDER BY p.id",
                (rs, i) -> new ProjectView(rs.getLong("id"), rs.getString("name"),
                        rs.getLong("client_id"), rs.getString("client_name")));
        response.send(projects);
    }
}
