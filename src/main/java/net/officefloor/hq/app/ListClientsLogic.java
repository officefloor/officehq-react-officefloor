package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients — the active (not archived) clients, oldest first. */
public class ListClientsLogic {

    public void service(ClientRepository clients, ObjectResponse<List<Client>> response) {
        response.send(clients.findByArchivedFalseOrderByIdAsc());
    }
}
