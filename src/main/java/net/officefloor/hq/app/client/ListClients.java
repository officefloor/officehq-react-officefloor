package net.officefloor.hq.app.client;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients — every client still in play (archived clients are tucked away). */
public class ListClients {

    public void service(ClientRepository repository, ObjectResponse<List<Client>> response) {
        response.send(repository.findByArchivedFalse());
    }
}
