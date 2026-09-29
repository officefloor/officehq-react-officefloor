package net.officefloor.hq.app.project;

import java.util.Set;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/projects — add a project with a name for a chosen client. */
public class CreateProject {

    private static final Set<String> STATUSES = Set.of("ACTIVE", "ON_HOLD", "FINISHED");

    public void service(@RequestBody NewProject body, ProjectRepository repository,
            ObjectResponse<Project> response) {
        String name = body.getName() == null ? "" : body.getName().trim();
        if (name.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project requires a name");
        }
        if (body.getClientId() == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project requires a client");
        }
        String code = body.getCode() == null ? "" : body.getCode().trim();
        if (code.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project requires a code");
        }
        // Two projects cannot share a reference code; reject a duplicate before it reaches the
        // unique constraint (see V28__projects_code.sql).
        if (repository.existsByCode(code)) {
            throw new HttpException(HttpStatus.CONFLICT,
                    "A project with this code already exists");
        }
        // Default to ACTIVE when the request omits a status; reject anything unrecognised.
        String status = body.getStatus() == null ? "ACTIVE" : body.getStatus();
        if (!STATUSES.contains(status)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "Unknown project status");
        }
        Project project = new Project();
        project.setName(name);
        project.setCode(code);
        project.setClientId(body.getClientId());
        project.setStatus(status);
        response.send(repository.save(project));
    }
}
