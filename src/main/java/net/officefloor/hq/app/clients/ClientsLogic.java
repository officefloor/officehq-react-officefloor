package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients — list every client. */
public class ClientsLogic {

    public void service(ClientService clients, ObjectResponse<List<ClientView>> response) {
        List<ClientView> views = clients.list().stream()
                .map(c -> new ClientView(c.getId(), c.getName(), c.getEmail()))
                .toList();
        response.send(views);
    }
}
