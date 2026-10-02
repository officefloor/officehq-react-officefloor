package net.officefloor.hq.app;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/clients — add a client from the submitted name + email, echo back the saved row. */
public class CreateClientLogic {

    public void service(@RequestBody NewClient newClient, ClientRepository clients,
            ObjectResponse<Client> response) {
        Client saved = clients.save(new Client(newClient.getName(), newClient.getEmail()));
        response.send(saved);
    }
}
