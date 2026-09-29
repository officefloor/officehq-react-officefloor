package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects — list all projects (each with its client's name). With an optional
 * {@code ?clientId=<id>} it lists only the projects done for that client.
 */
public class ProjectsGetLogic {

    public void service(@HttpQueryParameter("clientId") String clientId,
            ProjectRepository repository, ObjectResponse<List<Project>> response) {
        if (clientId == null || clientId.isBlank()) {
            response.send(repository.findAll());
        } else {
            response.send(repository.findByClient(Long.parseLong(clientId.trim())));
        }
    }
}
