package net.officefloor.hq.app.search;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * GET /api/search?q=&lt;term&gt; — one search box that looks across both clients and projects,
 * returning the matches grouped by kind. The match is a case-insensitive substring on each entity's
 * name; archived clients are tucked away and excluded. An empty term matches nothing.
 */
public class Search {

    public void service(@HttpQueryParameter("q") String q, JdbcTemplate jdbc,
            ObjectResponse<SearchView> response) {
        String term = q == null ? "" : q.trim();
        if (term.isEmpty()) {
            response.send(new SearchView(List.of(), List.of()));
            return;
        }
        String like = "%" + term.toLowerCase() + "%";
        List<SearchClientView> clients = jdbc.query(
                "SELECT id, name, email FROM clients "
                        + "WHERE archived = FALSE AND LOWER(name) LIKE ? ORDER BY id",
                (rs, i) -> new SearchClientView(rs.getLong("id"), rs.getString("name"),
                        rs.getString("email")),
                like);
        List<SearchProjectView> projects = jdbc.query(
                "SELECT p.id, p.name, p.client_id, c.name AS client_name "
                        + "FROM projects p JOIN clients c ON c.id = p.client_id "
                        + "WHERE LOWER(p.name) LIKE ? ORDER BY p.id",
                (rs, i) -> new SearchProjectView(rs.getLong("id"), rs.getString("name"),
                        rs.getLong("client_id"), rs.getString("client_name")),
                like);
        response.send(new SearchView(clients, projects));
    }
}
