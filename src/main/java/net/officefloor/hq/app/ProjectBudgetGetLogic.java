package net.officefloor.hq.app;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects/budget?projectId=&lt;id&gt; — a project's budget, invoiced and remaining. */
public class ProjectBudgetGetLogic {

    public void service(@HttpQueryParameter("projectId") String projectId,
            ProjectRepository repository, ObjectResponse<ProjectBudget> response) {
        long id = projectId == null || projectId.isBlank() ? 0 : Long.parseLong(projectId.trim());
        response.send(repository.budgetSummary(id));
    }
}
