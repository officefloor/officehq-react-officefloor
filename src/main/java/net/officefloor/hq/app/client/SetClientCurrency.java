package net.officefloor.hq.app.client;

import net.officefloor.hq.app.Audit;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/{id}/currency — set the currency a client is paid in (the ISO code, e.g. USD,
 * EUR). Their money is then shown in that currency everywhere it appears. Records the change to the
 * audit file and returns the updated client.
 */
public class SetClientCurrency {

    public void service(@HttpPathParameter("id") String id, @RequestBody NewCurrency body,
            ClientRepository repository, Audit audit, ObjectResponse<Client> response) {
        String currency = body.getCurrency() == null ? "" : body.getCurrency().trim();
        if (currency.isEmpty()) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client requires a currency");
        }
        Client client = repository.findById(Long.valueOf(id))
                .orElseThrow(() -> new HttpException(HttpStatus.NOT_FOUND, "No such client"));
        client.setCurrency(currency);
        Client saved = repository.save(client);
        audit.record("CLIENT_CURRENCY_SET id=" + saved.getId() + " currency=" + currency);
        response.send(saved);
    }
}
