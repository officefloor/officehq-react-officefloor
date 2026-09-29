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
        return findByTag(tagId, includeArchived, null);
    }

    /**
     * Projects carrying a given tag, optionally narrowed to a single lifecycle {@code status}
     * (ACTIVE, ON_HOLD or FINISHED) — this lets the label filter and the status filter apply at the
     * same time (e.g. "active projects with the 'urgent' label"). A null/blank status lists every
     * status. Archived projects are hidden unless {@code includeArchived} is true.
     */
    public List<Project> findByTag(long tagId, boolean includeArchived, String status) {
        StringBuilder sql = new StringBuilder(SELECT
                + " JOIN project_tags pt ON pt.project_id = p.id WHERE pt.tag_id = ?");
        List<Object> args = new java.util.ArrayList<>();
        args.add(tagId);
        if (!includeArchived) {
            sql.append(" AND p.archived = FALSE");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status = ?");
            args.add(status.trim());
        }
        sql.append(" ORDER BY p.id");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    /** Projects done for one client — reused by the client detail view. Archived ones are hidden. */
    public List<Project> findByClient(long clientId) {
        return findByClient(clientId, false, null);
    }

    /**
     * Projects done for one client, optionally narrowed to a single lifecycle {@code status} (e.g.
     * ACTIVE) — this backs the client page showing only active projects by default. Archived
     * projects are hidden unless {@code includeArchived} is true (the "also show finished and
     * hidden" toggle reveals every status, archived included).
     */
    public List<Project> findByClient(long clientId, boolean includeArchived, String status) {
        StringBuilder sql = new StringBuilder(SELECT + " WHERE p.client_id = ?");
        List<Object> args = new java.util.ArrayList<>();
        args.add(clientId);
        if (!includeArchived) {
            sql.append(" AND p.archived = FALSE");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status = ?");
            args.add(status.trim());
        }
        sql.append(" ORDER BY p.id");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }

    /**
     * Projects whose name matches the global search term (case-insensitive substring). Archived
     * projects stay hidden, matching the plain list. A blank term matches nothing — the global
     * search only surfaces results once something is typed.
     */
    public List<Project> searchByName(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }
        return jdbc.query(
                SELECT + " WHERE p.archived = FALSE AND LOWER(p.name) LIKE ? ORDER BY p.id",
                MAPPER, "%" + term.trim().toLowerCase() + "%");
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

    /**
     * A project's budget summary: the agreed budget (null when none is set), how much has been
     * invoiced against it (the sum of its issued invoices' line-item totals — drafts are not yet
     * invoiced), and what is left (budget minus invoiced, null when there is no budget).
     */
    public ProjectBudget budgetSummary(long id) {
        java.math.BigDecimal budget = jdbc.queryForObject(
                "SELECT budget FROM projects WHERE id = ?", java.math.BigDecimal.class, id);
        java.math.BigDecimal invoiced = jdbc.queryForObject(
                "SELECT COALESCE(SUM(li.qty * li.unit_price), 0) FROM invoices i"
                        + " JOIN line_items li ON li.invoice_id = i.id"
                        + " WHERE i.project_id = ? AND i.status <> 'DRAFT'",
                java.math.BigDecimal.class, id);
        java.math.BigDecimal remaining = budget == null ? null : budget.subtract(invoiced);
        return new ProjectBudget(budget, invoiced, remaining);
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM projects WHERE id = ?", id) > 0;
    }

    /** Tuck a project away: flag it archived so it drops off the lists but its row is retained. */
    public boolean archive(long id) {
        return jdbc.update("UPDATE projects SET archived = TRUE WHERE id = ?", id) > 0;
    }
}
