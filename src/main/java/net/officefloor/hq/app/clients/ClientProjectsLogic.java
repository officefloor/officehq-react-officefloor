package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/{clientId}/projects — list the projects done for a client. Only the client's
 * ACTIVE, non-archived projects are returned by default; pass {@code ?all=true} to also include the
 * finished and hidden (archived) ones (the client page's "show all" toggle).
 */
public class ClientProjectsLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            @HttpQueryParameter("all") String all, ClientService clients,
            ObjectResponse<List<ClientProjectView>> response) {
        response.send(clients.projectsFor(Long.valueOf(clientId), Boolean.parseBoolean(all)));
    }
}
