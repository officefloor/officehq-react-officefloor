package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/{clientId}/projects — list the projects done for a client. */
public class ClientProjectsLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientService clients,
            ObjectResponse<List<ClientProjectView>> response) {
        response.send(clients.projectsFor(Long.valueOf(clientId)));
    }
}
