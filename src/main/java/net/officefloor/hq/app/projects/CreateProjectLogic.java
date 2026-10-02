package net.officefloor.hq.app.projects;

import net.officefloor.web.ObjectResponse;

/** POST /api/projects — add a project for a client from the JSON body and return the created row. */
public class CreateProjectLogic {

    public void service(NewProject body, ProjectService projects, ObjectResponse<ProjectView> response) {
        response.send(projects.create(body.getName(), body.getClientId(), body.getStatus()));
    }
}
