package net.officefloor.hq.app;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients/outstanding — how much each client still owes, for sorting the clients list. */
public class ClientsOutstandingGetLogic {

    public void service(ClientOutstandingRepository repository,
            ObjectResponse<List<ClientOutstanding>> response) {
        response.send(repository.outstandingByClient());
    }
}
