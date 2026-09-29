package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/archived — list the archived (tucked-away) clients so they can be brought back. */
public class ClientsArchivedGetLogic {

    public void service(ClientRepository repository, ObjectResponse<List<Client>> response) {
        response.send(repository.findArchived());
    }
}
