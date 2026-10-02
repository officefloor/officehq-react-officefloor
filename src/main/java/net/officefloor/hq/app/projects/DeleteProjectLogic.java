package net.officefloor.hq.app.projects;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/projects/{projectId}/delete — delete a project the user no longer needs and return the
 * remaining projects. The deletion is recorded in the audit log so there is a record it happened.
 */
public class DeleteProjectLogic {

    public void service(@HttpPathParameter("projectId") String projectId, ProjectService projects,
            ObjectResponse<List<ProjectView>> response) {
        response.send(projects.delete(Long.valueOf(projectId)));
    }
}
