package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects — list all projects (each with its client's name). With an optional
 * {@code ?clientId=<id>} it lists only the projects done for that client. Archived projects are
 * hidden unless {@code ?includeArchived=true} is passed (the "show archived" toggle).
 */
public class ProjectsGetLogic {

    public void service(@HttpQueryParameter("clientId") String clientId,
            @HttpQueryParameter("includeArchived") String includeArchived,
            ProjectRepository repository, ObjectResponse<List<Project>> response) {
        if (clientId == null || clientId.isBlank()) {
            boolean withArchived = includeArchived != null && "true".equalsIgnoreCase(includeArchived.trim());
            response.send(repository.findAll(withArchived));
        } else {
            response.send(repository.findByClient(Long.parseLong(clientId.trim())));
        }
    }
}
