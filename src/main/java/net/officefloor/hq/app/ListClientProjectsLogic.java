package net.officefloor.hq.app;

import java.util.List;
import java.util.stream.Collectors;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/{clientId}/projects — every project done for one client, oldest first, each
 * carrying its status and archived flag. The client's page shows only the active (status ACTIVE and
 * not archived) ones by default and lets the user reveal the finished and hidden ones; it filters
 * this full list client-side, so the endpoint returns them all.
 */
public class ListClientProjectsLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            ProjectRepository projects, ClientRepository clients,
            ObjectResponse<List<ProjectView>> response) {
        Long id = Long.valueOf(clientId);
        String clientName = clients.findById(id).map(Client::getName).orElse(null);
        List<ProjectView> views = projects.findByClientIdOrderByIdAsc(id).stream()
                .map(p -> new ProjectView(p.getId(), p.getName(), p.getClientId(), clientName,
                        p.isArchived(), p.getStatus(), List.of()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
