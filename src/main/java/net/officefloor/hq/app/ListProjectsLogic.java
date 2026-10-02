package net.officefloor.hq.app;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects — all projects, oldest first, each carrying its client's name (not just id). */
public class ListProjectsLogic {

    public void service(ProjectRepository projects, ClientRepository clients,
            ObjectResponse<List<ProjectView>> response) {
        Map<Long, String> clientNames = clients.findAll().stream()
                .collect(Collectors.toMap(Client::getId, Client::getName));
        List<ProjectView> views = projects.findAllByOrderByIdAsc().stream()
                .map(p -> new ProjectView(p.getId(), p.getName(), p.getClientId(),
                        clientNames.get(p.getClientId())))
                .collect(Collectors.toList());
        response.send(views);
    }
}
