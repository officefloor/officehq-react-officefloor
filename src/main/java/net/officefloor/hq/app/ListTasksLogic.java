package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects/{projectId}/tasks — a project's checklist of tasks, oldest first. Returns every
 * task by default; {@code ?status=OPEN} narrows to the not-yet-done tasks and {@code ?status=DONE}
 * to the finished ones.
 */
public class ListTasksLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            @HttpQueryParameter("status") String status, TaskRepository tasks,
            ObjectResponse<List<TaskView>> response) {
        Long id = Long.valueOf(projectId);
        List<Task> found;
        if ("OPEN".equals(status)) {
            found = tasks.findByProjectIdAndDoneOrderByIdAsc(id, false);
        } else if ("DONE".equals(status)) {
            found = tasks.findByProjectIdAndDoneOrderByIdAsc(id, true);
        } else {
            found = tasks.findByProjectIdOrderByIdAsc(id);
        }
        List<TaskView> views = found.stream()
                .map(t -> new TaskView(t.getId(), t.getProjectId(), t.getTitle(), t.isDone()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
