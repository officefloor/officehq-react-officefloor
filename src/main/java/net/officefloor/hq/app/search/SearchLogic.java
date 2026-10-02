package net.officefloor.hq.app.search;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/search — one search term looked up across both clients and projects by name, returning
 * the matches grouped by kind. An empty {@code q} yields empty groups.
 */
public class SearchLogic {

    public void service(@HttpQueryParameter("q") String q, SearchService search,
            ObjectResponse<SearchView> response) {
        response.send(search.search(q));
    }
}
