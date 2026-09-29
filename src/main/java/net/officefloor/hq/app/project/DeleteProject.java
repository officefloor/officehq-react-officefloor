package net.officefloor.hq.app.project;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/projects/{id}/delete — remove a project the user no longer needs, and record the fact to
 * the audit file so the deletion can be checked back later. Returns the removed project so the caller
 * can confirm what went.
 */
public class DeleteProject {

    public void service(@HttpPathParameter("id") String id, ProjectRepository repository,
            Audit audit, ObjectResponse<ProjectView> response) {
        Long projectId = Long.valueOf(id);
        Project project = repository.findById(projectId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such project"));
        ProjectView removed = new ProjectView(project.getId(), project.getName(),
                project.getClientId(), null);
        repository.deleteById(projectId);
        audit.record("PROJECT_DELETED id=" + projectId);
        response.send(removed);
    }
}
