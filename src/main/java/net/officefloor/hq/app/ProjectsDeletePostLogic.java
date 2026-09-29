package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/projects/delete — delete a project no longer needed. Removes the row and records one
 * audit line (PROJECT_DELETED id=&lt;id&gt;) so the deletion is kept on record.
 */
public class ProjectsDeletePostLogic {

    public void service(@RequestBody DeleteProject body, ProjectRepository repository, Audit audit,
            ObjectResponse<Project> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project id is required.");
        }
        Project existing = repository.findById(body.getId());
        if (existing == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such project.");
        }
        repository.delete(existing.id());
        audit.record("PROJECT_DELETED id=" + existing.id());
        response.send(existing);
    }
}
