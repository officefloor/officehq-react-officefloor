package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/** GET /api/clients — list clients, optionally filtered by a case-insensitive name search ({@code q}). */
public class ClientsLogic {

    public void service(@HttpQueryParameter("q") String q, ClientService clients,
            ObjectResponse<List<ClientView>> response) {
        List<ClientView> views = clients.search(q).stream()
                .map(c -> new ClientView(c.getId(), c.getName(), c.getEmail()))
                .toList();
        response.send(views);
    }
}
