package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects — list all projects (each with its client's name). */
public class ProjectsGetLogic {

    public void service(ProjectRepository repository, ObjectResponse<List<Project>> response) {
        response.send(repository.findAll());
    }
}
