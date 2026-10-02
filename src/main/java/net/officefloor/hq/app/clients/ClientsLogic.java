package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients — list clients, optionally filtered by a case-insensitive name search ({@code q})
 * and ordered by {@code sort}: {@code name} (A–Z) or {@code outstanding} (most owed first). Pass
 * {@code archived=true} to instead list the clients tucked away (the "show archived" view).
 */
public class ClientsLogic {

    public void service(@HttpQueryParameter("q") String q, @HttpQueryParameter("sort") String sort,
            @HttpQueryParameter("archived") String archived, ClientService clients,
            ObjectResponse<List<ClientView>> response) {
        if ("true".equals(archived)) {
            response.send(clients.listArchived());
        } else {
            response.send(clients.listSorted(q, sort));
        }
    }
}
