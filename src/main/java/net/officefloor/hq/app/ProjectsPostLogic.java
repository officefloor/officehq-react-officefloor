package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/projects — create a project for a client from the request body. */
public class ProjectsPostLogic {

    public void service(@RequestBody NewProject body, ProjectRepository repository,
            ObjectResponse<Project> response) {
        String name = body.getName() == null ? "" : body.getName().trim();
        if (name.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A project name is required.");
        }
        if (body.getClientId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client is required.");
        }
        response.send(repository.create(name, body.getClientId()));
    }
}
