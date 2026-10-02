package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/{clientId}/projects — the projects done for one client, oldest first. */
public class ListClientProjectsLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            ProjectRepository projects, ClientRepository clients,
            ObjectResponse<List<ProjectView>> response) {
        Long id = Long.valueOf(clientId);
        String clientName = clients.findById(id).map(Client::getName).orElse(null);
        List<ProjectView> views = projects.findByClientIdOrderByIdAsc(id).stream()
                .map(p -> new ProjectView(p.getId(), p.getName(), p.getClientId(), clientName))
                .collect(Collectors.toList());
        response.send(views);
    }
}
