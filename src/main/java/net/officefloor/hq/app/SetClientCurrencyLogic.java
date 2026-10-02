package net.officefloor.hq.app;

import java.util.Set;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * PUT /api/clients/{clientId}/currency — set the currency the owner is paid in by one client, so the
 * client's money is shown in it everywhere. Echoes back the saved client. An unknown client is
 * rejected with 400 and nothing is written; a currency the app does not support is rejected with 400.
 */
public class SetClientCurrencyLogic {

    /** The currencies the app knows how to show (matching the UI's choices). */
    private static final Set<String> SUPPORTED = Set.of("USD", "EUR");

    public void service(@HttpPathParameter("clientId") String clientId,
            @RequestBody NewClientCurrency body, ClientRepository clients,
            ObjectResponse<Client> response) {
        Long id = Long.valueOf(clientId);
        Client client = clients.findById(id).orElseThrow(
                () -> new HttpException(HttpStatus.BAD_REQUEST, "An existing client is required"));
        String currency = body.getCurrency();
        if (currency == null || !SUPPORTED.contains(currency)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A supported currency is required");
        }
        client.setCurrency(currency);
        response.send(clients.save(client));
    }
}
