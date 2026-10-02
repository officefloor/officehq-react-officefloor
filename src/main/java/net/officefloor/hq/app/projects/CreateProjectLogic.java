package net.officefloor.hq.app.projects;

import net.officefloor.web.ObjectResponse;

/** POST /api/projects — add a project for a client from the JSON body and return the result. */
public class CreateProjectLogic {

    public void service(NewProject body, ProjectService projects,
            ObjectResponse<CreateProjectResult> response) {
        try {
            response.send(CreateProjectResult.created(
                    projects.create(body.getName(), body.getClientId(), body.getStatus(),
                            body.getCode())));
        } catch (DuplicateProjectCodeException e) {
            // Code already in use: no row added; the UI surfaces the code error.
            response.send(CreateProjectResult.codeInUse());
        }
    }
}
