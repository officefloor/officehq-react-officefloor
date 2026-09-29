package net.officefloor.hq.app.client;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/clients — add a client with a name and email. */
public class CreateClient {

    public void service(@RequestBody NewClient body, ClientRepository repository,
            ObjectResponse<Client> response) {
        Client client = new Client();
        client.setName(body.getName());
        client.setEmail(body.getEmail());
        response.send(repository.save(client));
    }
}
