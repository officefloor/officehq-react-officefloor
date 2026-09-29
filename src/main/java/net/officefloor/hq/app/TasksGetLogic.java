package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/tasks?projectId=&lt;id&gt; — list a project's tasks (checklist). */
public class TasksGetLogic {

    public void service(@HttpQueryParameter("projectId") String projectId,
            TaskRepository repository, ObjectResponse<List<Task>> response) {
        long id = projectId == null || projectId.isBlank() ? 0 : Long.parseLong(projectId.trim());
        response.send(repository.findByProject(id));
    }
}
