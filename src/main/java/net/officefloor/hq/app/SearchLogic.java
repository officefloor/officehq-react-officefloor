package net.officefloor.hq.app;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/search?q=... — one search box across the whole book. Matches active clients and active
 * projects whose name contains the query (case-insensitive), grouped by kind. A blank query matches
 * nothing, so the box starts empty.
 */
public class SearchLogic {

    public void service(@HttpQueryParameter("q") String q, ClientRepository clients,
            ProjectRepository projects, ObjectResponse<SearchView> response) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) {
            response.send(new SearchView(List.of(), List.of()));
            return;
        }
        List<Client> matchedClients =
                clients.findByArchivedFalseAndNameContainingIgnoreCaseOrderByIdAsc(query);
        Map<Long, String> clientNames = clients.findAll().stream()
                .collect(Collectors.toMap(Client::getId, Client::getName));
        List<ProjectView> matchedProjects =
                projects.findByArchivedFalseAndNameContainingIgnoreCaseOrderByIdAsc(query).stream()
                        .map(p -> new ProjectView(p.getId(), p.getName(), p.getClientId(),
                                clientNames.get(p.getClientId())))
                        .collect(Collectors.toList());
        response.send(new SearchView(matchedClients, matchedProjects));
    }
}
