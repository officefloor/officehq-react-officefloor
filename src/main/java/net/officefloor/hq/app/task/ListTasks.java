package net.officefloor.hq.app.task;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.jdbc.core.JdbcTemplate;

/** GET /api/tasks?projectId=<id> — every task on one project, oldest first. */
public class ListTasks {

    public void service(@HttpQueryParameter("projectId") String projectId, JdbcTemplate jdbc,
            ObjectResponse<List<TaskView>> response) {
        List<TaskView> tasks = jdbc.query(
                "SELECT id, project_id, title, done FROM tasks WHERE project_id = ? ORDER BY id",
                (rs, i) -> new TaskView(rs.getLong("id"), rs.getLong("project_id"),
                        rs.getString("title"), rs.getBoolean("done")),
                Long.valueOf(projectId));
        response.send(tasks);
    }
}
