package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/** POST /api/clients/{clientId}/contacts — add a contact for the client and return the row. */
public class CreateClientContactLogic {

    public void service(@HttpPathParameter("clientId") String clientId, NewContact body,
            ClientService clients, ObjectResponse<ClientContactView> response) {
        response.send(clients.createContact(Long.valueOf(clientId), body.getName(),
                body.getEmail(), body.getRole()));
    }
}
