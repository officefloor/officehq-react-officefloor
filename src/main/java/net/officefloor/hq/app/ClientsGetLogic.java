package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients — list all clients. */
public class ClientsGetLogic {

    public void service(ClientRepository repository, ObjectResponse<List<Client>> response) {
        response.send(repository.findAll());
    }
}
