package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Project} rows (JdbcTemplate over the schema in V3__projects.sql). */
@Repository
public class ProjectRepository {

    private final JdbcTemplate jdbc;

    public ProjectRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final org.springframework.jdbc.core.RowMapper<Project> MAPPER =
            (rs, i) -> new Project(rs.getLong("id"), rs.getString("name"),
                    rs.getLong("client_id"), rs.getString("client_name"));

    public List<Project> findAll() {
        return jdbc.query(
                "SELECT p.id, p.name, p.client_id, c.name AS client_name"
                        + " FROM projects p JOIN clients c ON p.client_id = c.id"
                        + " ORDER BY p.id",
                MAPPER);
    }

    /** Projects done for one client — reused by the client detail view. */
    public List<Project> findByClient(long clientId) {
        return jdbc.query(
                "SELECT p.id, p.name, p.client_id, c.name AS client_name"
                        + " FROM projects p JOIN clients c ON p.client_id = c.id"
                        + " WHERE p.client_id = ? ORDER BY p.id",
                MAPPER, clientId);
    }

    public Project create(String name, long clientId) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO projects (name, client_id) VALUES (?, ?)", new String[] {"id"});
            ps.setString(1, name);
            ps.setLong(2, clientId);
            return ps;
        }, keys);
        String clientName = jdbc.queryForObject(
                "SELECT name FROM clients WHERE id = ?", String.class, clientId);
        return new Project(keys.getKey().longValue(), name, clientId, clientName);
    }
}
