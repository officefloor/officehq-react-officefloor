package net.officefloor.hq.app.projects;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/{projectId}/budget — a project's budget, invoiced total and what is left. */
public class ProjectBudgetLogic {

    public void service(@HttpPathParameter("projectId") String projectId, ProjectService projects,
            ObjectResponse<ProjectBudgetView> response) {
        response.send(projects.budgetFor(Long.valueOf(projectId)));
    }
}
