package net.officefloor.hq.app;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.officefloor.web.ObjectResponse;

/** GET /api/projects — all projects, oldest first, each carrying its client's name (not just id). */
public class ListProjectsLogic {

    public void service(ProjectRepository projects, ClientRepository clients,
            ProjectTagRepository projectTags, ObjectResponse<List<ProjectView>> response) {
        Map<Long, String> clientNames = clients.findAll().stream()
                .collect(Collectors.toMap(Client::getId, Client::getName));
        Map<Long, List<Long>> tagIdsByProject = projectTags.findAll().stream()
                .collect(Collectors.groupingBy(ProjectTag::getProjectId,
                        Collectors.mapping(ProjectTag::getTagId, Collectors.toList())));
        List<ProjectView> views = projects.findAllByOrderByIdAsc().stream()
                .map(p -> new ProjectView(p.getId(), p.getName(), p.getClientId(),
                        clientNames.get(p.getClientId()), p.isArchived(), p.getStatus(),
                        tagIdsByProject.getOrDefault(p.getId(), List.of()), p.getCode()))
                .collect(Collectors.toList());
        response.send(views);
    }
}
