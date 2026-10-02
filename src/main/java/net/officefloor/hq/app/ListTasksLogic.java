package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/{projectId}/tasks — a project's checklist of tasks, oldest first. */
public class ListTasksLogic {

    public void service(@HttpPathParameter("projectId") String projectId, TaskRepository tasks,
            ObjectResponse<List<TaskView>> response) {
        Long id = Long.valueOf(projectId);
        List<TaskView> views = tasks.findByProjectIdOrderByIdAsc(id).stream()
                .map(t -> new TaskView(t.getId(), t.getProjectId(), t.getTitle(), t.isDone()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
