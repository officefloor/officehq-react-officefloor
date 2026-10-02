package net.officefloor.hq.app.search;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for the global search: one query term looked up across both clients and projects
 * by name (case-insensitive, substring). Archived rows are tucked away, so they drop off the search
 * just as they do from each feature's own list. Reads both tables via SQL so the search feature
 * stays self-contained and does not import the clients or projects features' Java types.
 */
@Service
public class SearchService {

    private final JdbcTemplate jdbc;

    public SearchService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Clients and projects whose name contains {@code query}; empty when the term is blank. */
    @Transactional(readOnly = true)
    public SearchView search(String query) {
        if (query == null || query.isBlank()) {
            return new SearchView(List.of(), List.of());
        }
        String like = "%" + query.trim().toLowerCase() + "%";

        List<SearchClientView> clients = jdbc.query(
                "SELECT id, name, email FROM clients "
                        + "WHERE archived = FALSE AND LOWER(name) LIKE ? ORDER BY id ASC",
                (rs, i) -> new SearchClientView(rs.getLong("id"), rs.getString("name"),
                        rs.getString("email")),
                like);

        List<SearchProjectView> projects = jdbc.query(
                "SELECT p.id, p.name, p.client_id, c.name AS client_name "
                        + "FROM projects p JOIN clients c ON p.client_id = c.id "
                        + "WHERE p.archived = FALSE AND LOWER(p.name) LIKE ? ORDER BY p.id ASC",
                (rs, i) -> new SearchProjectView(rs.getLong("id"), rs.getString("name"),
                        rs.getLong("client_id"), rs.getString("client_name")),
                like);

        return new SearchView(clients, projects);
    }
}
