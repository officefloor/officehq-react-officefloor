package net.officefloor.hq.app.projects;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/projects/{projectId}/archive — tuck a project away rather than deleting it. The row is
 * kept but flagged archived so it drops off the lists; the action is recorded in the audit log. The
 * remaining (non-archived) projects are returned.
 */
public class ArchiveProjectLogic {

    public void service(@HttpPathParameter("projectId") String projectId, ProjectService projects,
            ObjectResponse<List<ProjectView>> response) {
        response.send(projects.archive(Long.valueOf(projectId)));
    }
}
