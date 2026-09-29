package net.officefloor.hq.app.project;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/projects — add a project with a name for a chosen client. */
public class CreateProject {

    public void service(@RequestBody NewProject body, ProjectRepository repository,
            ObjectResponse<Project> response) {
        String name = body.getName() == null ? "" : body.getName().trim();
        if (name.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project requires a name");
        }
        if (body.getClientId() == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project requires a client");
        }
        Project project = new Project();
        project.setName(name);
        project.setClientId(body.getClientId());
        response.send(repository.save(project));
    }
}
