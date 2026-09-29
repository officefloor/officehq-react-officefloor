package net.officefloor.hq.app;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Data access for {@link Task} rows (JdbcTemplate over the schema in V11__tasks.sql). */
@Repository
public class TaskRepository {

    private final JdbcTemplate jdbc;

    public TaskRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final org.springframework.jdbc.core.RowMapper<Task> MAPPER =
            (rs, i) -> new Task(rs.getLong("id"), rs.getLong("project_id"),
                    rs.getString("title"), rs.getBoolean("done"));

    public List<Task> findByProject(long projectId) {
        return jdbc.query(
                "SELECT id, project_id, title, done FROM tasks"
                        + " WHERE project_id = ? ORDER BY id",
                MAPPER, projectId);
    }

    /** A project's tasks filtered to just the open ({@code done=false}) or done ones. */
    public List<Task> findByProjectAndDone(long projectId, boolean done) {
        return jdbc.query(
                "SELECT id, project_id, title, done FROM tasks"
                        + " WHERE project_id = ? AND done = ? ORDER BY id",
                MAPPER, projectId, done);
    }

    public Task findById(long id) {
        List<Task> found = jdbc.query(
                "SELECT id, project_id, title, done FROM tasks WHERE id = ?", MAPPER, id);
        return found.isEmpty() ? null : found.get(0);
    }

    /** Set a task's done flag and return the updated row (null if no such task). */
    public Task setDone(long id, boolean done) {
        jdbc.update("UPDATE tasks SET done = ? WHERE id = ?", done, id);
        return findById(id);
    }

    public Task create(long projectId, String title) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO tasks (project_id, title) VALUES (?, ?)", new String[] {"id"});
            ps.setLong(1, projectId);
            ps.setString(2, title);
            return ps;
        }, keys);
        return new Task(keys.getKey().longValue(), projectId, title, false);
    }
}
