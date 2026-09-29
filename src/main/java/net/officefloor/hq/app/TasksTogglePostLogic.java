package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/tasks/toggle — tick a task off / on. Flips its done flag and records one audit line
 * (TASK_DONE / TASK_REOPENED id=&lt;id&gt;) so the change can be checked back later.
 */
public class TasksTogglePostLogic {

    public void service(@RequestBody ToggleTask body, TaskRepository repository, Audit audit,
            ObjectResponse<Task> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A task id is required.");
        }
        Task task = repository.findById(body.getId());
        if (task == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such task.");
        }
        Task updated = repository.setDone(task.id(), !task.done());
        audit.record((updated.done() ? "TASK_DONE" : "TASK_REOPENED") + " id=" + updated.id());
        response.send(updated);
    }
}
