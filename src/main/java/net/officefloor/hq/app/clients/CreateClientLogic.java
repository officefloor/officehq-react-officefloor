package net.officefloor.hq.app.clients;

import net.officefloor.web.ObjectResponse;

/** POST /api/clients — add a client from the JSON body and return the created row. */
public class CreateClientLogic {

    public void service(NewClient body, ClientService clients, ObjectResponse<ClientView> response) {
        Client saved = clients.create(body.getName(), body.getEmail());
        response.send(new ClientView(saved.getId(), saved.getName(), saved.getEmail()));
    }
}
