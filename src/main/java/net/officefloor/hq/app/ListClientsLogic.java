package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients — all clients, oldest first. */
public class ListClientsLogic {

    public void service(ClientRepository clients, ObjectResponse<List<Client>> response) {
        response.send(clients.findAllByOrderByIdAsc());
    }
}
