package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/projects — add a project for the chosen client, echo back the saved row with the
 * client's name. A project must name an existing client; an unknown or missing client id is
 * rejected with 400 and never persisted.
 */
public class CreateProjectLogic {

    public void service(@RequestBody NewProject newProject, ProjectRepository projects,
            ClientRepository clients, ObjectResponse<ProjectView> response) {
        Long clientId = newProject.getClientId();
        if (clientId == null) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client is required");
        }
        Client client = clients.findById(clientId).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "A client is required"));
        Project saved = projects.save(new Project(newProject.getName(), clientId));
        response.send(new ProjectView(saved.getId(), saved.getName(), client.getId(),
                client.getName()));
    }
}
