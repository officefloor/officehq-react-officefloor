package net.officefloor.hq.app.clients;

import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

/**
 * POST /api/clients/{clientId}/currency — set the currency a client is paid in, so their money is
 * shown in it everywhere. Returns the updated client.
 */
public class SetClientCurrencyLogic {

    public void service(@HttpPathParameter("clientId") String clientId, NewCurrency body,
            ClientService clients, ObjectResponse<ClientView> response) {
        response.send(clients.setCurrency(Long.valueOf(clientId), body.getCurrency()));
    }
}
