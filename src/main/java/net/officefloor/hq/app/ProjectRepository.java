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
                    rs.getLong("client_id"), rs.getString("client_name"),
                    rs.getBoolean("archived"), rs.getString("status"));

    private static final String SELECT =
            "SELECT p.id, p.name, p.client_id, p.archived, p.status, c.name AS client_name"
                    + " FROM projects p JOIN clients c ON p.client_id = c.id";

    /**
     * List projects. Archived projects are hidden unless {@code includeArchived} is true — that is
     * what keeps a tucked-away project off the main list until the toggle reveals it.
     */
    public List<Project> findAll(boolean includeArchived) {
        return findAll(includeArchived, null);
    }

    /**
     * List projects, optionally narrowed to a single lifecycle {@code status} (ACTIVE, ON_HOLD or
     * FINISHED) — this backs the "filter projects by status" dropdown. A null/blank status lists
     * every status. Archived projects are hidden unless {@code includeArchived} is true.
     */
    public List<Project> findAll(boolean includeArchived, String status) {
        StringBuilder sql = new StringBuilder(SELECT);
        List<Object> args = new java.util.ArrayList<>();
        String joiner = " WHERE";
        if (!includeArchived) {
            sql.append(joiner).append(" p.archived = FALSE");
            joiner = " AND";
        }
        if (status != null && !status.isBlank()) {
            sql.append(joiner).append(" p.status = ?");
            args.add(status.trim());
        }
        sql.append(" ORDER BY p.id");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    /**
     * Projects carrying a given tag — backs the "filter my projects by label" dropdown. Archived
     * projects are hidden unless {@code includeArchived} is true, matching the plain list.
     */
    public List<Project> findByTag(long tagId, boolean includeArchived) {
        String archived = includeArchived ? "" : " AND p.archived = FALSE";
        return jdbc.query(
                SELECT + " JOIN project_tags pt ON pt.project_id = p.id"
                        + " WHERE pt.tag_id = ?" + archived + " ORDER BY p.id",
                MAPPER, tagId);
    }

    /** Projects done for one client — reused by the client detail view. Archived ones are hidden. */
    public List<Project> findByClient(long clientId) {
        return jdbc.query(SELECT + " WHERE p.client_id = ? AND p.archived = FALSE ORDER BY p.id",
                MAPPER, clientId);
    }

    public Project create(String name, long clientId, String status) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO projects (name, client_id, status) VALUES (?, ?, ?)",
                    new String[] {"id"});
            ps.setString(1, name);
            ps.setLong(2, clientId);
            ps.setString(3, status);
            return ps;
        }, keys);
        String clientName = jdbc.queryForObject(
                "SELECT name FROM clients WHERE id = ?", String.class, clientId);
        return new Project(keys.getKey().longValue(), name, clientId, clientName, false, status);
    }

    /** Look up one project (with joined client name), or null when there is no such row. */
    public Project findById(long id) {
        List<Project> rows = jdbc.query(SELECT + " WHERE p.id = ?", MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM projects WHERE id = ?", id) > 0;
    }

    /** Tuck a project away: flag it archived so it drops off the lists but its row is retained. */
    public boolean archive(long id) {
        return jdbc.update("UPDATE projects SET archived = TRUE WHERE id = ?", id) > 0;
    }
}
