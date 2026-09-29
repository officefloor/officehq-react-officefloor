package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/tasks?projectId=&lt;id&gt;&amp;status=&lt;ALL|OPEN|DONE&gt; — list a project's tasks
 * (checklist), optionally filtered to just the open or just the done ones. An absent or unknown
 * {@code status} lists them all.
 */
public class TasksGetLogic {

    public void service(@HttpQueryParameter("projectId") String projectId,
            @HttpQueryParameter("status") String status,
            TaskRepository repository, ObjectResponse<List<Task>> response) {
        long id = projectId == null || projectId.isBlank() ? 0 : Long.parseLong(projectId.trim());
        String filter = status == null ? "" : status.trim().toUpperCase();
        if ("OPEN".equals(filter)) {
            response.send(repository.findByProjectAndDone(id, false));
        } else if ("DONE".equals(filter)) {
            response.send(repository.findByProjectAndDone(id, true));
        } else {
            response.send(repository.findByProject(id));
        }
    }
}
