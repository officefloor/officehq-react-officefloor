package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/{clientId}/statement — a statement for a client: all of their invoices gathered
 * in one place, with the total they still owe.
 */
public class ClientStatementLogic {

    public void service(@HttpPathParameter("clientId") String clientId, ClientService clients,
            ObjectResponse<ClientStatementView> response) {
        response.send(clients.statementFor(Long.valueOf(clientId)));
    }
}
