package net.officefloor.hq.app;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/summary?clientId=&lt;id&gt; — the project and contact counts for one client,
 * shown at a glance on that client's detail view.
 */
public class ClientSummaryGetLogic {

    public void service(@HttpQueryParameter("clientId") String clientId,
            ClientSummaryRepository repository, ObjectResponse<ClientSummary> response) {
        response.send(repository.forClient(Long.parseLong(clientId.trim())));
    }
}
