package net.officefloor.hq.app.tasks;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects/{projectId}/tasks — list the tasks on a project's task list, optionally
 * narrowed to just the open or just the finished ones via the {@code filter} query parameter.
 */
public class TasksLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            @HttpQueryParameter("filter") String filter, TaskService tasks,
            ObjectResponse<List<TaskView>> response) {
        response.send(tasks.listForProject(Long.valueOf(projectId), filter));
    }
}
