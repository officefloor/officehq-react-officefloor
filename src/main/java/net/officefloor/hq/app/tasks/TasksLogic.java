package net.officefloor.hq.app.tasks;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/{projectId}/tasks — list the tasks on a project's task list. */
public class TasksLogic {

    public void service(@HttpPathParameter("projectId") String projectId, TaskService tasks,
            ObjectResponse<List<TaskView>> response) {
        response.send(tasks.listForProject(Long.valueOf(projectId)));
    }
}
