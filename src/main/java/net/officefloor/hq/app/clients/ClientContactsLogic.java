package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/{clientId}/contacts — list the contacts kept for a client. */
public class ClientContactsLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientService clients,
            ObjectResponse<List<ClientContactView>> response) {
        response.send(clients.contactsFor(Long.valueOf(clientId)));
    }
}
