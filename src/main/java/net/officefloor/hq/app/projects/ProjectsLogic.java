package net.officefloor.hq.app.projects;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects — list every project with its client's name. */
public class ProjectsLogic {

    public void service(ProjectService projects, ObjectResponse<List<ProjectView>> response) {
        response.send(projects.list());
    }
}
