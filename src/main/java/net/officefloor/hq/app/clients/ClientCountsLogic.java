package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/{clientId}/counts — how many projects and contacts a client has. */
public class ClientCountsLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientService clients,
            ObjectResponse<ClientCountsView> response) {
        response.send(clients.countsFor(Long.valueOf(clientId)));
    }
}
