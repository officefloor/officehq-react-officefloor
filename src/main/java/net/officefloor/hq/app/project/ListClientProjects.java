package net.officefloor.hq.app.project;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/clients/{id}/projects?scope=&lt;active|all&gt; — a client's projects, oldest first. By
 * default (scope absent or {@code active}) only the client's ACTIVE, non-archived projects are
 * returned; {@code scope=all} also includes the finished and hidden (archived) ones.
 */
public class ListClientProjects {

    public void service(@HttpPathParameter("id") String id,
            @HttpQueryParameter("scope") String scope, JdbcTemplate jdbc,
            ObjectResponse<List<ProjectView>> response) {
        boolean all = "all".equalsIgnoreCase(scope);
        String sql = "SELECT p.id, p.name, p.code, p.client_id, p.archived, p.status, "
                + "c.name AS client_name "
                + "FROM projects p JOIN clients c ON c.id = p.client_id WHERE p.client_id = ?";
        if (!all) {
            sql += " AND p.archived = FALSE AND p.status = 'ACTIVE'";
        }
        sql += " ORDER BY p.id";
        List<ProjectView> projects = jdbc.query(sql,
                (rs, i) -> new ProjectView(rs.getLong("id"), rs.getString("name"),
                        rs.getString("code"), rs.getLong("client_id"), rs.getString("client_name"),
                        rs.getBoolean("archived"), rs.getString("status")),
                Long.valueOf(id));
        response.send(projects);
    }
}
