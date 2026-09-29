package net.officefloor.hq.app;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/search?q=&lt;term&gt; — the one global search box. It looks across both clients and
 * projects, matching each by name (case-insensitive substring) and returning the matches grouped by
 * kind. A blank term returns empty groups.
 */
public class SearchGetLogic {

    public void service(@HttpQueryParameter("q") String q,
            ClientRepository clients, ProjectRepository projects,
            ObjectResponse<SearchResults> response) {
        response.send(new SearchResults(clients.searchByName(q), projects.searchByName(q)));
    }
}
