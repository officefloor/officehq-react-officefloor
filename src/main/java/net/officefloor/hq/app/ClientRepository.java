package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Client} rows (JdbcTemplate over the H2 schema in V1__clients.sql). */
@Repository
public class ClientRepository {

    private final JdbcTemplate jdbc;

    public ClientRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * List clients. Archived clients are hidden — that is what keeps a tucked-away client off the
     * main list (and the name search that filters it) while its row is retained.
     */
    public List<Client> findAll() {
        return jdbc.query(
                "SELECT id, name, email FROM clients WHERE archived = FALSE ORDER BY id",
                (rs, i) -> new Client(rs.getLong("id"), rs.getString("name"), rs.getString("email")));
    }

    /**
     * Clients whose name matches the global search term (case-insensitive substring). Archived
     * clients stay hidden, matching the plain list. A blank term matches nothing — the global
     * search only surfaces results once something is typed.
     */
    public List<Client> searchByName(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }
        return jdbc.query(
                "SELECT id, name, email FROM clients"
                        + " WHERE archived = FALSE AND LOWER(name) LIKE ? ORDER BY id",
                (rs, i) -> new Client(rs.getLong("id"), rs.getString("name"), rs.getString("email")),
                "%" + term.trim().toLowerCase() + "%");
    }

    /** Look up one client, or null when there is no such row. */
    public Client findById(long id) {
        List<Client> rows = jdbc.query(
                "SELECT id, name, email FROM clients WHERE id = ?",
                (rs, i) -> new Client(rs.getLong("id"), rs.getString("name"), rs.getString("email")),
                id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** Tuck a client away: flag it archived so it drops off the lists but its row is retained. */
    public boolean archive(long id) {
        return jdbc.update("UPDATE clients SET archived = TRUE WHERE id = ?", id) > 0;
    }

    public Client create(String name, String email) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO clients (name, email) VALUES (?, ?)", new String[] {"id"});
            ps.setString(1, name);
            ps.setString(2, email);
            return ps;
        }, keys);
        return new Client(keys.getKey().longValue(), name, email);
    }
}
