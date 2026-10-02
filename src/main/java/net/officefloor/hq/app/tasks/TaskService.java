package net.officefloor.hq.app.tasks;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for a project's task list. Tasks are always viewed in the context of their project,
 * so the list is scoped by project id and returned in a stable order. Ticking a task off flips its
 * done flag (OPEN <-> DONE) and returns the updated row.
 */
@Service
public class TaskService {

    private final JdbcTemplate jdbc;

    public TaskService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * A project's tasks in a stable order, optionally narrowed to just the OPEN (not done) or just
     * the DONE (ticked off) ones. A blank/absent {@code filter} lists every task.
     */
    @Transactional(readOnly = true)
    public List<TaskView> listForProject(Long projectId, String filter) {
        RowMapper<TaskView> mapper = (rs, i) -> new TaskView(rs.getLong("id"),
                rs.getLong("project_id"), rs.getString("title"), rs.getBoolean("done"));
        String base = "SELECT id, project_id, title, done FROM tasks WHERE project_id = ? ";
        String narrowed = filter == null ? "" : filter.trim().toUpperCase();
        if ("OPEN".equals(narrowed)) {
            return jdbc.query(base + "AND done = FALSE ORDER BY id ASC", mapper, projectId);
        }
        if ("DONE".equals(narrowed)) {
            return jdbc.query(base + "AND done = TRUE ORDER BY id ASC", mapper, projectId);
        }
        return jdbc.query(base + "ORDER BY id ASC", mapper, projectId);
    }

    @Transactional
    public TaskView toggle(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("A task id is required");
        }
        jdbc.update("UPDATE tasks SET done = NOT done WHERE id = ?", taskId);
        return find(taskId);
    }

    private TaskView find(Long taskId) {
        return jdbc.queryForObject(
                "SELECT id, project_id, title, done FROM tasks WHERE id = ?",
                (rs, i) -> new TaskView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getString("title"), rs.getBoolean("done")),
                taskId);
    }
}
