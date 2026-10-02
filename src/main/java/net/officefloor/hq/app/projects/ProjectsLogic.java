package net.officefloor.hq.app.projects;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/projects — list projects with their client's name. Archived projects are hidden by
 * default; pass {@code ?includeArchived=true} to reveal them (the UI's "show archived" toggle).
 */
public class ProjectsLogic {

    public void service(@HttpQueryParameter("includeArchived") String includeArchived,
            ProjectService projects, ObjectResponse<List<ProjectView>> response) {
        response.send(projects.list(Boolean.parseBoolean(includeArchived)));
    }
}
