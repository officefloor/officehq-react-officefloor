package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/archived — the archived (tucked-away) clients, oldest first. */
public class ListArchivedClientsLogic {

    public void service(ClientRepository clients, ObjectResponse<List<Client>> response) {
        response.send(clients.findByArchivedTrueOrderByIdAsc());
    }
}
