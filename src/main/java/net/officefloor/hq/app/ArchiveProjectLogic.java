package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/projects/{projectId}/archive — tuck a project away rather than deleting it. Sets the
 * project's archived flag so it drops off the project list and the client's list while being
 * retained, appends one audit record (PROJECT_ARCHIVED id=&lt;id&gt;) so there is a record it
 * happened, and echoes back the saved project. An unknown project is rejected with 400 and nothing
 * is written.
 */
public class ArchiveProjectLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            ProjectRepository projects, ClientRepository clients, Audit audit,
            ObjectResponse<ProjectView> response) {
        Long id = Long.valueOf(projectId);
        Project project = projects.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing project is required"));
        project.setArchived(true);
        Project saved = projects.save(project);
        String clientName = clients.findById(saved.getClientId()).map(Client::getName).orElse(null);
        audit.record("PROJECT_ARCHIVED id=" + id);
        response.send(new ProjectView(saved.getId(), saved.getName(), saved.getClientId(),
                clientName, saved.isArchived()));
    }
}
