package net.officefloor.hq.app.project;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/projects/{id}/archive — tuck a project away instead of deleting it: flag it archived so
 * it drops off the main project list and the client's project list, but nothing is lost (it can be
 * revealed again). Records the fact to the audit file so it can be checked back later.
 */
public class ArchiveProject {

    public void service(@HttpPathParameter("id") String id, ProjectRepository repository,
            Audit audit, ObjectResponse<ProjectView> response) {
        Long projectId = Long.valueOf(id);
        Project project = repository.findById(projectId)
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such project"));
        project.setArchived(true);
        Project saved = repository.save(project);
        audit.record("PROJECT_ARCHIVED id=" + saved.getId());
        response.send(new ProjectView(saved.getId(), saved.getName(), saved.getClientId(), null,
                saved.isArchived(), saved.getStatus()));
    }
}
