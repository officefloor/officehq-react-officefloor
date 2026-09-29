package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects — list all projects (each with its client's name). With an optional
 * {@code ?clientId=<id>} it lists only the projects done for that client; with {@code ?tagId=<id>}
 * only the projects carrying that tag (the "filter by label" dropdown). Archived projects are
 * hidden unless {@code ?includeArchived=true} is passed (the "show archived" toggle).
 */
public class ProjectsGetLogic {

    public void service(@HttpQueryParameter("clientId") String clientId,
            @HttpQueryParameter("tagId") String tagId,
            @HttpQueryParameter("includeArchived") String includeArchived,
            @HttpQueryParameter("status") String status,
            ProjectRepository repository, ObjectResponse<List<Project>> response) {
        boolean withArchived = includeArchived != null && "true".equalsIgnoreCase(includeArchived.trim());
        if (tagId != null && !tagId.isBlank()) {
            response.send(repository.findByTag(Long.parseLong(tagId.trim()), withArchived));
        } else if (clientId == null || clientId.isBlank()) {
            response.send(repository.findAll(withArchived, status));
        } else {
            response.send(repository.findByClient(Long.parseLong(clientId.trim())));
        }
    }
}
