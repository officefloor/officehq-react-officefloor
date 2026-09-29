package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/projects/archive — tuck a project away instead of deleting it. Flags the row archived so
 * it drops off the main project list and the client's page, but keeps the row (a toggle reveals
 * archived projects). Records one audit line (PROJECT_ARCHIVED id=&lt;id&gt;) so it is noted.
 */
public class ProjectsArchivePostLogic {

    public void service(@RequestBody ArchiveProject body, ProjectRepository repository, Audit audit,
            ObjectResponse<Project> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project id is required.");
        }
        Project existing = repository.findById(body.getId());
        if (existing == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such project.");
        }
        repository.archive(existing.id());
        audit.record("PROJECT_ARCHIVED id=" + existing.id());
        response.send(new Project(existing.id(), existing.name(), existing.clientId(),
                existing.clientName(), true));
    }
}
