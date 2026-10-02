package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * DELETE /api/projects/{projectId} — remove a project the owner no longer needs, then append one
 * audit record (PROJECT_DELETED id=&lt;id&gt;) so there is a record it happened, and echo back the
 * removed project. An unknown project is rejected with 400 and nothing is written.
 */
public class DeleteProjectLogic {

    public void service(@HttpPathParameter("projectId") String projectId,
            ProjectRepository projects, ClientRepository clients, Audit audit,
            ObjectResponse<ProjectView> response) {
        Long id = Long.valueOf(projectId);
        Project project = projects.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing project is required"));
        String clientName = clients.findById(project.getClientId()).map(Client::getName).orElse(null);
        projects.deleteById(id);
        audit.record("PROJECT_DELETED id=" + id);
        response.send(new ProjectView(id, project.getName(), project.getClientId(), clientName));
    }
}
