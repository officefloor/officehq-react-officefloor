package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{clientId}/restore — bring a client back from the archive. Clears the archived
 * flag so the client returns to the main list and search; the action is recorded in the audit log.
 * The clients that remain archived are returned.
 */
public class RestoreClientLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientService clients,
            ObjectResponse<List<ClientView>> response) {
        response.send(clients.restore(Long.valueOf(clientId)));
    }
}
