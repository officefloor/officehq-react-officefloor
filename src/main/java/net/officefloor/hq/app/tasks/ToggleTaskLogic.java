package net.officefloor.hq.app.tasks;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/tasks/{taskId}/toggle — tick a task off (or back on) and return the updated row. */
public class ToggleTaskLogic {

    public void service(@HttpPathParameter("taskId") String taskId, TaskService tasks,
            ObjectResponse<TaskView> response) {
        response.send(tasks.toggle(Long.valueOf(taskId)));
    }
}
