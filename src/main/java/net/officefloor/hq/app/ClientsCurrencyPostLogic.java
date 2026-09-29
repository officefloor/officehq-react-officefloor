package net.officefloor.hq.app;

import java.util.Set;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * POST /api/clients/currency — set the currency a client is paid in. Their money is then shown in
 * that currency everywhere. Records one audit line (CLIENT_CURRENCY_SET id=&lt;id&gt;
 * currency=&lt;code&gt;).
 */
public class ClientsCurrencyPostLogic {

    // The currencies the app supports. Kept in step with the front-end select.
    private static final Set<String> SUPPORTED = Set.of("USD", "EUR");

    public void service(@RequestBody SetClientCurrency body, ClientRepository repository,
            Audit audit, ObjectResponse<CurrencySetting> response) {
        if (body.getId() <= 0) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "A client id is required.");
        }
        String currency = body.getCurrency() == null ? "" : body.getCurrency().trim().toUpperCase();
        if (!SUPPORTED.contains(currency)) {
            throw new HttpException(HttpStatus.BAD_REQUEST, "An unsupported currency.");
        }
        Client updated = repository.updateCurrency(body.getId(), currency);
        if (updated == null) {
            throw new HttpException(HttpStatus.NOT_FOUND, "No such client.");
        }
        audit.record("CLIENT_CURRENCY_SET id=" + updated.id() + " currency=" + currency);
        response.send(new CurrencySetting(updated.id(), updated.currency()));
    }
}
