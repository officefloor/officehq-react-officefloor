package net.officefloor.hq.app;

import java.util.Set;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/projects — create a project for a client from the request body. */
public class ProjectsPostLogic {

    /** The lifecycle states a project may be created in; defaults to ACTIVE when omitted. */
    private static final Set<String> STATUSES = Set.of("ACTIVE", "ON_HOLD", "FINISHED");

    public void service(@RequestBody NewProject body, ProjectRepository repository,
            ObjectResponse<Project> response) {
        String name = body.getName() == null ? "" : body.getName().trim();
        if (name.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project name is required.");
        }
        if (body.getClientId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client is required.");
        }
        String status = body.getStatus() == null ? "ACTIVE" : body.getStatus().trim();
        if (status.isEmpty()) {
            status = "ACTIVE";
        }
        if (!STATUSES.contains(status)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Unknown project status: " + status);
        }
        String code = body.getCode() == null ? "" : body.getCode().trim();
        if (code.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A job code is required.");
        }
        // No two jobs may share a code — reject the create when the code is already in use (mirrors
        // the projects_code_unique constraint). CONFLICT so the front-end can flag the code field.
        if (repository.existsByCode(code)) {
            throw new HttpException(HttpStatus.CONFLICT, "A job with this code already exists.");
        }
        response.send(repository.create(name, body.getClientId(), status, code));
    }
}
