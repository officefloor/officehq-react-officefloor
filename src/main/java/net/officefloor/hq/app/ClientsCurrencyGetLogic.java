package net.officefloor.hq.app;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;

/**
 * GET /api/clients/currency?clientId=&lt;id&gt; — the currency a client is paid in, so the client's
 * panels can show and change it.
 */
public class ClientsCurrencyGetLogic {

    public void service(@HttpQueryParameter("clientId") String clientId,
            ClientRepository repository, ObjectResponse<CurrencySetting> response) {
        Client client = repository.findById(Long.parseLong(clientId.trim()));
        if (client == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such client.");
        }
        response.send(new CurrencySetting(client.id(), client.currency()));
    }
}
