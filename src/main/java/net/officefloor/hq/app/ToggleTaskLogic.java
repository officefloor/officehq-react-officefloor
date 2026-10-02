package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/tasks/{taskId}/toggle — tick a task off (or back on). Flips the task's done flag between
 * OPEN and DONE, records the transition, and echoes back the saved row. An unknown task is rejected
 * with 400 and nothing is written.
 */
public class ToggleTaskLogic {

    public void service(@HttpPathParameter("taskId") String taskId, TaskRepository tasks, Audit audit,
            ObjectResponse<TaskView> response) {
        Long id = Long.valueOf(taskId);
        Task task = tasks.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing task is required"));
        task.setDone(!task.isDone());
        Task saved = tasks.save(task);
        audit.record("TASK_TOGGLED id=" + saved.getId() + " done=" + saved.isDone());
        response.send(new TaskView(saved.getId(), saved.getProjectId(), saved.getTitle(), saved.isDone()));
    }
}
