package net.officefloor.hq.app;

import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/statement?clientId=&lt;id&gt; — a client's statement: all of that client's
 * invoices in one place, with the total still owed across them.
 */
public class ClientStatementGetLogic {

    public void service(@HttpQueryParameter("clientId") String clientId,
            ClientStatementRepository repository, ObjectResponse<ClientStatement> response) {
        response.send(repository.forClient(Long.parseLong(clientId.trim())));
    }
}
