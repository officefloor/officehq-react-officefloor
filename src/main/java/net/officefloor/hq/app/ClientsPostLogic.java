package net.officefloor.hq.app;

import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/** POST /api/clients — create a client from the request body. */
public class ClientsPostLogic {

    public void service(@RequestBody NewClient body, ClientRepository repository,
            ObjectResponse<Client> response) {
        response.send(repository.create(body.getName(), body.getEmail()));
    }
}
