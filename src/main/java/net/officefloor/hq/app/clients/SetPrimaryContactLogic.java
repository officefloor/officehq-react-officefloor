package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{clientId}/contacts/{contactId}/primary — pick the client's one main contact.
 * Returns the client's contacts with the chosen one flagged primary.
 */
public class SetPrimaryContactLogic {

    public void service(@HttpPathParameter("clientId") String clientId,
            @HttpPathParameter("contactId") String contactId, ClientService clients,
            ObjectResponse<List<ClientContactView>> response) {
        response.send(clients.setPrimaryContact(Long.valueOf(clientId), Long.valueOf(contactId)));
    }
}
