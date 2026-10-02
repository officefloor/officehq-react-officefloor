package net.officefloor.hq.app.clients;

import java.util.List;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients — list clients, optionally filtered by a case-insensitive name search ({@code q})
 * and ordered by {@code sort}: {@code name} (A–Z) or {@code outstanding} (most owed first).
 */
public class ClientsLogic {

    public void service(@HttpQueryParameter("q") String q, @HttpQueryParameter("sort") String sort,
            ClientService clients, ObjectResponse<List<ClientView>> response) {
        response.send(clients.listSorted(q, sort));
    }
}
