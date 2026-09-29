package net.officefloor.hq.app.task;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/tasks/{id}/toggle — tick a task off or reopen it: flip its done flag and record the
 * fact to the audit file so it can be checked back later.
 */
public class ToggleTask {

    public void service(@HttpPathParameter("id") String id, TaskRepository repository,
            Audit audit, ObjectResponse<TaskView> response) {
        Long taskId = Long.valueOf(id);
        Task task = repository.findById(taskId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such task"));
        task.setDone(!task.isDone());
        Task saved = repository.save(task);
        audit.record((saved.isDone() ? "TASK_DONE" : "TASK_REOPENED") + " id=" + saved.getId());
        response.send(new TaskView(saved.getId(), saved.getProjectId(), saved.getTitle(),
                saved.isDone()));
    }
}
