package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/{clientId} — one client, including the currency they are paid in. */
public class GetClientLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientService clients,
            ObjectResponse<ClientView> response) {
        response.send(clients.get(Long.valueOf(clientId)));
    }
}
